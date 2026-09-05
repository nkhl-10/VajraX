# VAJRAX: Supabase Backend Setup & Database Schema

Follow these instructions to connect your Supabase project with VAJRAX for cloud backup and multi-device synchronization.

---

## 1. Required Supabase Credentials
You need two values from your Supabase Dashboard (**Project Settings → API**):
1. **Project URL**: `https://<your-project-id>.supabase.co`
2. **Anon Public Key**: `eyJhbGciOiJIUzI1NiIsInR5cCI6...`

---

## 2. SQL Schema for Supabase (Copy & Paste in SQL Editor)
Go to **Supabase Dashboard → SQL Editor → New Query** and paste the following SQL to create the cloud tables with Row Level Security (RLS):

```sql
-- 1. Profiles Table
CREATE TABLE public.profiles (
    id UUID REFERENCES auth.users ON DELETE CASCADE PRIMARY KEY,
    email TEXT NOT NULL,
    display_name TEXT,
    active_path_id TEXT DEFAULT 'high_performance',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Practices Table (User's Daily System)
CREATE TABLE public.practices (
    id TEXT PRIMARY KEY,
    user_id UUID REFERENCES auth.users ON DELETE CASCADE NOT NULL,
    title TEXT NOT NULL,
    target_duration_minutes INT NOT NULL,
    minimum_duration_minutes INT NOT NULL,
    preferred_time TEXT,
    tracking_mode TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Action Records (Timeline Execution)
CREATE TABLE public.action_records (
    id TEXT PRIMARY KEY,
    user_id UUID REFERENCES auth.users ON DELETE CASCADE NOT NULL,
    practice_id TEXT REFERENCES public.practices(id) ON DELETE CASCADE NOT NULL,
    date DATE NOT NULL,
    scheduled_time TEXT,
    status TEXT NOT NULL,
    completed_at TIMESTAMPTZ,
    duration_minutes INT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. Evidence Logs
CREATE TABLE public.evidence_logs (
    id TEXT PRIMARY KEY,
    user_id UUID REFERENCES auth.users ON DELETE CASCADE NOT NULL,
    action_record_id TEXT REFERENCES public.action_records(id) ON DELETE CASCADE NOT NULL,
    reflection_rating TEXT,
    note TEXT,
    media_path TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Book Ideas (Book to Life)
CREATE TABLE public.book_ideas (
    id TEXT PRIMARY KEY,
    user_id UUID REFERENCES auth.users ON DELETE CASCADE NOT NULL,
    book_title TEXT NOT NULL,
    author TEXT NOT NULL,
    idea TEXT NOT NULL,
    practical_application TEXT NOT NULL,
    is_experiment_active BOOLEAN DEFAULT FALSE,
    experiment_days_left INT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==========================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==========================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.practices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.action_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.evidence_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.book_ideas ENABLE ROW LEVEL SECURITY;

-- Profiles Policy
CREATE POLICY "Users can manage own profile" ON public.profiles
    FOR ALL USING (auth.uid() = id);

-- Practices Policy
CREATE POLICY "Users can manage own practices" ON public.practices
    FOR ALL USING (auth.uid() = user_id);

-- Action Records Policy
CREATE POLICY "Users can manage own action records" ON public.action_records
    FOR ALL USING (auth.uid() = user_id);

-- Evidence Logs Policy
CREATE POLICY "Users can manage own evidence" ON public.evidence_logs
    FOR ALL USING (auth.uid() = user_id);

-- Book Ideas Policy
CREATE POLICY "Users can manage own book ideas" ON public.book_ideas
    FOR ALL USING (auth.uid() = user_id);
```

---

## 3. How Sync Works (Offline-First Architecture)
1. VAJRAX always writes instantly to local **SQLDelight** (Zero latency, full offline capability).
2. When internet is available, `SupabaseSyncManager` pushes local delta changes to Supabase in the background using Ktor / Supabase client.
