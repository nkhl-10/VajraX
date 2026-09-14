-- ========================================================================
-- LUMINA LIFE OS: Production-Grade Supabase Cloud PostgreSQL Schema
-- Tables: profiles, templates, practices, action_records, evidence, user_templates
-- Includes Row Level Security (RLS) & Performance Indexes
-- ========================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    display_name TEXT NOT NULL DEFAULT 'Lumina User',
    avatar_initials TEXT NOT NULL DEFAULT 'LU',
    active_template_id TEXT,
    theme_mode TEXT NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.templates (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    task_count INTEGER NOT NULL DEFAULT 5,
    frequency TEXT NOT NULL DEFAULT 'Daily',
    author TEXT,
    is_community BOOLEAN NOT NULL DEFAULT FALSE,
    is_bookmarked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.practices (
    id TEXT PRIMARY KEY,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    template_id TEXT REFERENCES public.templates(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    target_duration_minutes INTEGER NOT NULL DEFAULT 30,
    minimum_duration_minutes INTEGER NOT NULL DEFAULT 5,
    preferred_time TEXT,
    tracking_mode TEXT NOT NULL DEFAULT 'MANUAL',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.action_records (
    id TEXT PRIMARY KEY,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    practice_id TEXT REFERENCES public.practices(id) ON DELETE CASCADE,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    scheduled_time TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING',
    completed_at TIMESTAMPTZ,
    duration_minutes INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.evidence (
    id TEXT PRIMARY KEY,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    action_record_id TEXT REFERENCES public.action_records(id) ON DELETE CASCADE,
    reflection_rating TEXT,
    note TEXT,
    media_path TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.user_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    template_id TEXT REFERENCES public.templates(id) ON DELETE CASCADE,
    current_day INTEGER NOT NULL DEFAULT 1,
    total_days INTEGER NOT NULL DEFAULT 30,
    progress_percent INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, template_id)
);

CREATE INDEX IF NOT EXISTS idx_practices_user ON public.practices(user_id);
CREATE INDEX IF NOT EXISTS idx_action_records_user_date ON public.action_records(user_id, date);
CREATE INDEX IF NOT EXISTS idx_action_records_practice ON public.action_records(practice_id);
CREATE INDEX IF NOT EXISTS idx_evidence_record ON public.evidence(action_record_id);
CREATE INDEX IF NOT EXISTS idx_user_templates_user ON public.user_templates(user_id);

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.templates ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.practices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.action_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.evidence ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_templates ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own profile" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can insert own profile" ON public.profiles FOR INSERT WITH CHECK (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);

CREATE POLICY "Anyone can view templates" ON public.templates FOR SELECT USING (true);
CREATE POLICY "Authenticated users can insert community templates" ON public.templates FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Authors can update own templates" ON public.templates FOR UPDATE USING (auth.role() = 'authenticated');

CREATE POLICY "Users can view own practices" ON public.practices FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own practices" ON public.practices FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own practices" ON public.practices FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own practices" ON public.practices FOR DELETE USING (auth.uid() = user_id);

CREATE POLICY "Users can view own action records" ON public.action_records FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own action records" ON public.action_records FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own action records" ON public.action_records FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own action records" ON public.action_records FOR DELETE USING (auth.uid() = user_id);

CREATE POLICY "Users can view own evidence" ON public.evidence FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own evidence" ON public.evidence FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own evidence" ON public.evidence FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own evidence" ON public.evidence FOR DELETE USING (auth.uid() = user_id);

CREATE POLICY "Users can view own user_templates" ON public.user_templates FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own user_templates" ON public.user_templates FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own user_templates" ON public.user_templates FOR UPDATE USING (auth.uid() = user_id);

INSERT INTO public.templates (id, title, description, task_count, frequency, author, is_community, is_bookmarked)
VALUES 
('morning_discipline', 'Morning Discipline', 'Build a structured morning routine', 6, 'Daily', NULL, FALSE, FALSE),
('deep_work_block', 'Deep Work Block', 'Lock in focus and maximize high-output hours', 4, 'Daily', NULL, FALSE, FALSE),
('30_day_challenge', '30-Day Challenge', 'A rigorous month-long discipline protocol', 8, '30 days', NULL, FALSE, FALSE),
('6am_routine', '6 AM Routine', 'Wake up early and win the morning', 5, 'Daily', NULL, FALSE, FALSE),
('evening_wind_down', 'Evening Wind Down', 'Slow down your mind for deep, restful recovery', 4, 'Daily', 'sarah', TRUE, TRUE),
('fitness_starter_pack', 'Fitness Starter Pack', 'Essential daily habits for athletic consistency', 5, 'Daily', 'mike', TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;
