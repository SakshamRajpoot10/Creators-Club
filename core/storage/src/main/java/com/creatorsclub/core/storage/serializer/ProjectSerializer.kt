package com.creatorsclub.core.storage.serializer

import android.graphics.Bitmap
import com.creatorsclub.core.storage.model.ProjectManifest
import kotlinx.serialization.json.Json
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ProjectSerializer {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Packs a project directory containing assets and manifest into a single .krv zip archive.
     */
    fun packProject(
        projectDir: File,
        manifest: ProjectManifest,
        thumbnailBitmap: Bitmap?,
        outputKrvFile: File
    ): Result<File> = runCatching {
        outputKrvFile.parentFile?.mkdirs()

        // 1. Write manifest.json locally inside projectDir
        val manifestFile = File(projectDir, "manifest.json")
        manifestFile.writeText(json.encodeToString(ProjectManifest.serializer(), manifest))

        // 2. Write thumbnail.webp if provided
        thumbnailBitmap?.let { bitmap ->
            val thumbFile = File(projectDir, "thumbnail.webp")
            FileOutputStream(thumbFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.WEBP, 85, out)
            }
        }

        // 3. Zip all files into .krv archive
        ZipOutputStream(BufferedOutputStream(FileOutputStream(outputKrvFile))).use { zos ->
            zipFolderRecursive(projectDir, projectDir, zos)
        }

        outputKrvFile
    }

    /**
     * Unpacks a .krv zip archive into destination directory with strict Zip-Slip protection.
     */
    fun unpackProject(
        krvFile: File,
        destinationDir: File
    ): Result<ProjectManifest> = runCatching {
        if (!krvFile.exists()) {
            throw FileNotFoundException("Project archive not found: ${krvFile.absolutePath}")
        }

        destinationDir.mkdirs()
        val canonicalDestPath = destinationDir.canonicalPath

        ZipFile(krvFile).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val targetFile = File(destinationDir, entry.name)

                // Critical security check: prevent directory traversal (Zip Slip vulnerability)
                val canonicalTargetPath = targetFile.canonicalPath
                if (!canonicalTargetPath.startsWith(canonicalDestPath)) {
                    throw SecurityException("Illegal zip path traversal detected: ${entry.name}")
                }

                if (entry.isDirectory) {
                    targetFile.mkdirs()
                } else {
                    targetFile.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }

        val manifestFile = File(destinationDir, "manifest.json")
        if (!manifestFile.exists()) {
            throw IllegalStateException("Corrupted project archive: missing manifest.json")
        }

        val manifestJson = manifestFile.readText()
        json.decodeFromString(ProjectManifest.serializer(), manifestJson)
    }

    private fun zipFolderRecursive(rootFolder: File, currentFolder: File, zos: ZipOutputStream) {
        val files = currentFolder.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                zipFolderRecursive(rootFolder, file, zos)
            } else {
                val relativePath = rootFolder.toURI().relativize(file.toURI()).path
                val entry = ZipEntry(relativePath)
                zos.putNextEntry(entry)
                FileInputStream(file).use { fis ->
                    fis.copyTo(zos)
                }
                zos.closeEntry()
            }
        }
    }
}
