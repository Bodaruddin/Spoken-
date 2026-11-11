-- =========================================================================
-- SPEAKLY - SUPABASE DATABASE FIX & RLS PERMISSIONS
-- Copy and run this entire script in your Supabase SQL Editor:
-- Dashboard -> SQL Editor -> New Query -> Paste & Run
-- =========================================================================

-- 1. Fix RLS on existing tables so Anon/Publishable key has full permissions
ALTER TABLE IF EXISTS students ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public students" ON students;
CREATE POLICY "Allow public students" ON students FOR ALL USING (true) WITH CHECK (true);

ALTER TABLE IF EXISTS teachers ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public teachers" ON teachers;
CREATE POLICY "Allow public teachers" ON teachers FOR ALL USING (true) WITH CHECK (true);

ALTER TABLE IF EXISTS student_progress ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public student_progress" ON student_progress;
CREATE POLICY "Allow public student_progress" ON student_progress FOR ALL USING (true) WITH CHECK (true);

ALTER TABLE IF EXISTS speaking_evaluations ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public speaking_evals" ON speaking_evaluations;
CREATE POLICY "Allow public speaking_evals" ON speaking_evaluations FOR ALL USING (true) WITH CHECK (true);

-- Fix RLS policy on assignments (previously blocked INSERT with 401 error)
ALTER TABLE IF EXISTS assignments ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public assignments" ON assignments;
CREATE POLICY "Allow public assignments" ON assignments FOR ALL USING (true) WITH CHECK (true);

-- 2. English & Assamese Vocabulary Words Table
CREATE TABLE IF NOT EXISTS vocabulary_words (
    id TEXT PRIMARY KEY,
    word TEXT NOT NULL,
    phonetic TEXT,
    part_of_speech TEXT DEFAULT 'noun',
    meaning TEXT,
    example_sentence TEXT,
    category TEXT DEFAULT 'General',
    is_favorite BOOLEAN DEFAULT FALSE,
    is_mastered BOOLEAN DEFAULT FALSE,
    assamese_meaning TEXT,
    assamese_transliteration TEXT,
    syllables TEXT,
    coaching_tip TEXT,
    added_by_teacher TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE vocabulary_words ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public vocabulary_words" ON vocabulary_words;
CREATE POLICY "Allow public vocabulary_words" ON vocabulary_words FOR ALL USING (true) WITH CHECK (true);

-- 3. Game Challenges Table (Sentence Builder, Word Match, Missing Letters, etc.)
CREATE TABLE IF NOT EXISTS game_challenges (
    id TEXT PRIMARY KEY,
    type TEXT NOT NULL,
    title_or_word TEXT NOT NULL,
    hint_or_meaning TEXT,
    category TEXT DEFAULT 'General',
    options JSONB,
    correct_index INT DEFAULT 0,
    extra_data TEXT,
    created_by TEXT DEFAULT 'Faculty',
    created_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE game_challenges ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public game_challenges" ON game_challenges;
CREATE POLICY "Allow public game_challenges" ON game_challenges FOR ALL USING (true) WITH CHECK (true);

-- 4. Pronunciation Drills Table (Words & Sentences for Speaking Practice)
CREATE TABLE IF NOT EXISTS pronunciation_drills (
    id TEXT PRIMARY KEY,
    word_or_sentence TEXT NOT NULL,
    ipa_phonetic TEXT,
    audio_focus TEXT,
    difficulty TEXT DEFAULT 'Medium',
    tips TEXT,
    created_by TEXT DEFAULT 'Faculty',
    created_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE pronunciation_drills ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public pronunciation_drills" ON pronunciation_drills;
CREATE POLICY "Allow public pronunciation_drills" ON pronunciation_drills FOR ALL USING (true) WITH CHECK (true);

-- 5. AI Speaking Prompts Table
CREATE TABLE IF NOT EXISTS speaking_prompts (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    category TEXT DEFAULT 'General',
    question TEXT NOT NULL,
    follow_up_tip TEXT,
    keywords JSONB,
    min_seconds INT DEFAULT 20,
    created_by TEXT DEFAULT 'Faculty',
    created_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE speaking_prompts ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public speaking_prompts" ON speaking_prompts;
CREATE POLICY "Allow public speaking_prompts" ON speaking_prompts FOR ALL USING (true) WITH CHECK (true);

-- 6. English Adventure Progress Table
CREATE TABLE IF NOT EXISTS student_adventure_progress (
    student_id TEXT PRIMARY KEY,
    current_world TEXT,
    completed_levels INT DEFAULT 0,
    total_stars INT DEFAULT 0,
    total_coins INT DEFAULT 0,
    total_xp INT DEFAULT 0,
    last_updated TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE student_adventure_progress ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public adventure" ON student_adventure_progress;
CREATE POLICY "Allow public adventure" ON student_adventure_progress FOR ALL USING (true) WITH CHECK (true);
