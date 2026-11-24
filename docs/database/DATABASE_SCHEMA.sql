-- ==============================================================================
-- CREATORS CLUB / KREAVILO STUDIO — SUPABASE & POSTGRESQL PRODUCTION DATABASE SCHEMA
-- ==============================================================================
-- Designed for 100% offline-first Android/iOS creative studio with optional cloud sync.
-- Follows strict non-negotiable guidelines:
-- - Zero subscription gates or paywalls
-- - RLS (Row Level Security) enforced on every table
-- - Multi-studio support: Photo, Video, Design, Drawing, Product, Interior, Writing, Audio, AR Tracing
-- ==============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ------------------------------------------------------------------------------
-- 1. PROFILES TABLE (Linked with auth.users)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT,
    avatar_url TEXT,
    bio TEXT,
    website TEXT,
    storage_used_bytes BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update their own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

-- ------------------------------------------------------------------------------
-- 2. PROJECTS TABLE (Unified multi-studio project catalog)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    studio_type TEXT NOT NULL, -- 'photo', 'video', 'design', 'drawing', 'product', 'interior', 'writing', 'audio', 'ar_drawing'
    thumbnail_url TEXT,
    canvas_width INT DEFAULT 1080,
    canvas_height INT DEFAULT 1920,
    canvas_state JSONB NOT NULL DEFAULT '{}'::jsonb,
    is_favorite BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    version_number INT DEFAULT 1,
    client_local_id TEXT,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

ALTER TABLE public.projects ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can access their own projects"
    ON public.projects FOR ALL
    USING (auth.uid() = user_id);

CREATE INDEX IF NOT EXISTS idx_projects_user_studio ON public.projects(user_id, studio_type);
CREATE INDEX IF NOT EXISTS idx_projects_updated_at ON public.projects(user_id, updated_at DESC);

-- ------------------------------------------------------------------------------
-- 3. PROJECT VERSIONS TABLE (Rollback and version history snapshots)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.project_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES public.projects(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    snapshot_state JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

ALTER TABLE public.project_versions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their project versions"
    ON public.project_versions FOR SELECT
    USING (EXISTS (
        SELECT 1 FROM public.projects WHERE projects.id = project_versions.project_id AND projects.user_id = auth.uid()
    ));

CREATE POLICY "Users can insert their project versions"
    ON public.project_versions FOR INSERT
    WITH CHECK (EXISTS (
        SELECT 1 FROM public.projects WHERE projects.id = project_versions.project_id AND projects.user_id = auth.uid()
    ));

CREATE INDEX IF NOT EXISTS idx_project_versions ON public.project_versions(project_id, version_number DESC);

-- ------------------------------------------------------------------------------
-- 4. SAVED ASSETS TABLE (Fonts, audio, stickers, textures, logos)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.saved_assets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    asset_type TEXT NOT NULL, -- 'fonts', 'audio', 'stickers', 'banners', 'textures'
    file_name TEXT NOT NULL,
    storage_path TEXT NOT NULL,
    mime_type TEXT,
    file_size_bytes BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

ALTER TABLE public.saved_assets ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage their saved assets"
    ON public.saved_assets FOR ALL
    USING (auth.uid() = user_id);

CREATE INDEX IF NOT EXISTS idx_saved_assets_user_type ON public.saved_assets(user_id, asset_type);

-- ------------------------------------------------------------------------------
-- 5. CUSTOM PRESETS TABLE (Color grades, tone curves, custom brushes)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.custom_presets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    studio_type TEXT NOT NULL,
    preset_name TEXT NOT NULL,
    preset_data JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

ALTER TABLE public.custom_presets ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage their custom presets"
    ON public.custom_presets FOR ALL
    USING (auth.uid() = user_id);

-- ------------------------------------------------------------------------------
-- 6. FAVORITE TOOLS TABLE (Pinned shortcuts in quick studio bar)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.favorite_tools (
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    studio_type TEXT NOT NULL,
    tool_id TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    PRIMARY KEY (user_id, studio_type, tool_id)
);

ALTER TABLE public.favorite_tools ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage their favorite tools"
    ON public.favorite_tools FOR ALL
    USING (auth.uid() = user_id);

-- ------------------------------------------------------------------------------
-- 7. USER PREFERENCES TABLE
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.user_preferences (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    default_export_format TEXT DEFAULT 'PNG',
    default_export_quality INT DEFAULT 95,
    haptics_enabled BOOLEAN DEFAULT TRUE,
    telemetry_enabled BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

ALTER TABLE public.user_preferences ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage their preferences"
    ON public.user_preferences FOR ALL
    USING (auth.uid() = user_id);

-- ------------------------------------------------------------------------------
-- 8. AUTOMATIC UPDATED_AT TRIGGER FUNCTION
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = TIMEZONE('utc'::text, NOW());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER on_profile_updated
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

CREATE OR REPLACE TRIGGER on_project_updated
    BEFORE UPDATE ON public.projects
    FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

CREATE OR REPLACE TRIGGER on_user_preferences_updated
    BEFORE UPDATE ON public.user_preferences
    FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

-- ------------------------------------------------------------------------------
-- 9. AUTO-CREATE PROFILE ON USER SIGNUP TRIGGER
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, display_name, avatar_url)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.raw_user_meta_data->>'name', split_part(NEW.email, '@', 1)),
        NEW.raw_user_meta_data->>'avatar_url'
    );
    INSERT INTO public.user_preferences (user_id)
    VALUES (NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
