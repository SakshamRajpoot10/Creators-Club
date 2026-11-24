# Database Schema & Storage Architecture
## Kreavilo Studio (Creators Club)

---

## 1. Room Database Architecture

The local database (`kreavilo_studio.db`) is managed via AndroidX Room with SQLite FTS5 for instant search.

```
┌─────────────────────┐       1:N       ┌─────────────────────┐
│    projects_table   ├────────────────>│    assets_table     │
└──────────┬──────────┘                 └─────────────────────┘
           │
           │ 1:N
           ▼
┌─────────────────────┐
│ project_history_tbl │
└─────────────────────┘

┌─────────────────────┐                 ┌─────────────────────┐
│ feature_registry_tbl│                 │ assistant_chats_tbl │
└─────────────────────┘                 └─────────────────────┘
```

---

## 2. Room Entity Definitions

### 2.1 Project Entity (`projects_table`)
```sql
CREATE TABLE projects_table (
    project_id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    studio_type TEXT NOT NULL,          -- 'PHOTO', 'DESIGN', 'VIDEO', 'DRAWING', etc.
    created_at INTEGER NOT NULL,
    modified_at INTEGER NOT NULL,
    canvas_width INTEGER NOT NULL,
    canvas_height INTEGER NOT NULL,
    aspect_ratio TEXT NOT NULL,
    thumbnail_path TEXT NOT NULL,
    project_file_path TEXT NOT NULL,    -- Path to .krv archive
    is_favorite INTEGER NOT NULL DEFAULT 0,
    is_deleted INTEGER NOT NULL DEFAULT 0, -- Soft delete (trash bin)
    deleted_at INTEGER DEFAULT NULL
);

CREATE INDEX idx_projects_modified ON projects_table(modified_at DESC);
CREATE INDEX idx_projects_studio ON projects_table(studio_type);
```

### 2.2 Asset Entity (`assets_table`)
```sql
CREATE TABLE assets_table (
    asset_id TEXT PRIMARY KEY NOT NULL,
    project_id TEXT NOT NULL,
    original_uri TEXT NOT NULL,
    local_cached_path TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    file_size_bytes INTEGER NOT NULL,
    width INTEGER,
    height INTEGER,
    duration_ms INTEGER,
    created_at INTEGER NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects_table(project_id) ON DELETE CASCADE
);

CREATE INDEX idx_assets_project ON assets_table(project_id);
```

### 2.3 Feature Registry Entity (`feature_registry_tbl` with FTS5)
```sql
CREATE TABLE feature_registry_tbl (
    feature_id TEXT PRIMARY KEY NOT NULL,
    display_name TEXT NOT NULL,
    display_name_hi TEXT NOT NULL,
    aliases_csv TEXT NOT NULL,
    studio_category TEXT NOT NULL,
    navigation_route TEXT NOT NULL,
    required_inputs_json TEXT NOT NULL,
    action_type TEXT NOT NULL,
    help_steps_json TEXT NOT NULL
);

CREATE VIRTUAL TABLE feature_registry_fts USING fts5(
    feature_id UNINDEXED,
    display_name,
    display_name_hi,
    aliases_csv,
    content='feature_registry_tbl',
    content_rowid='rowid'
);
```

### 2.4 Assistant Conversation Entity (`assistant_chats_tbl`)
```sql
CREATE TABLE assistant_chats_tbl (
    message_id TEXT PRIMARY KEY NOT NULL,
    session_id TEXT NOT NULL,
    sender TEXT NOT NULL,                -- 'USER' or 'CREATOR_AI'
    text_content TEXT NOT NULL,
    suggested_action_id TEXT DEFAULT NULL,
    timestamp INTEGER NOT NULL
);

CREATE INDEX idx_chats_session ON assistant_chats_tbl(session_id, timestamp ASC);
```

---

## 3. Storage Hierarchy & Scoped Storage Strategy

```
Internal App Storage (context.filesDir)
├── database/
│   └── kreavilo_studio.db
├── ai_models/                          # Downloaded verified model weights
│   └── seg_interactive.tflite
└── preferences/
    └── user_settings.preferences_pb

External App Storage (context.getExternalFilesDir())
├── projects/                           # Editable .krv working packages
│   └── proj_9b1deb4d3b7d/
│       ├── manifest.json
│       ├── thumbnail.webp
│       └── assets/
└── temp_cache/                         # Transient render frames & waveforms
    └── (cleared automatically on app close)

User Public Storage (via MediaStore / SAF)
├── DCIM/Kreavilo/                      # Exported MP4 videos & Photos
└── Documents/Kreavilo/                 # Exported PDFs, TXT scripts, backups
```
