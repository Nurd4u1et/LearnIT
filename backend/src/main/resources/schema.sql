-- ============================================================================
-- Learnly: Online Learning Platform Schema (PostgreSQL)
-- Fully normalized, with foreign keys and indexes.
-- ============================================================================


-- ----------------------------------------------------------------------------
-- USERS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(180) NOT NULL UNIQUE,
    phone           VARCHAR(32)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER'   CHECK (role IN ('USER','ADMIN')),
    plan            VARCHAR(20)  NOT NULL DEFAULT 'FREE'   CHECK (plan IN ('FREE','PRO')),
    language        VARCHAR(5)   NOT NULL DEFAULT 'en'     CHECK (language IN ('en','ru')),
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);

-- ----------------------------------------------------------------------------
-- AUTH TOKENS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_refresh_user ON refresh_tokens(user_id);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- COURSES & LESSONS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courses (
    id           BIGSERIAL PRIMARY KEY,
    slug         VARCHAR(80)  NOT NULL UNIQUE,
    title        VARCHAR(200) NOT NULL,
    title_ru     VARCHAR(200),
    description  TEXT         NOT NULL,
    description_ru TEXT,
    language     VARCHAR(40)  NOT NULL,        -- e.g. 'Python','Java','Go'
    level        VARCHAR(20)  NOT NULL DEFAULT 'BEGINNER'
                 CHECK (level IN ('BEGINNER','INTERMEDIATE','ADVANCED')),
    cover_url    TEXT,
    is_pro_only  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_courses_language ON courses(language);

CREATE TABLE IF NOT EXISTS lessons (
    id            BIGSERIAL PRIMARY KEY,
    course_id     BIGINT       NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    position      INT          NOT NULL,
    title         VARCHAR(200) NOT NULL,
    title_ru      VARCHAR(200),
    video_url     TEXT,
    description   TEXT         NOT NULL,
    description_ru TEXT,
    task          TEXT         NOT NULL,
    task_ru       TEXT,
    expected_solution TEXT,                        -- optional reference solution
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (course_id, position)
);
CREATE INDEX IF NOT EXISTS idx_lessons_course ON lessons(course_id);

-- ----------------------------------------------------------------------------
-- ENROLLMENTS & PROGRESS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS enrollments (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    course_id   BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, course_id)
);
CREATE INDEX IF NOT EXISTS idx_enroll_user   ON enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_enroll_course ON enrollments(course_id);

CREATE TABLE IF NOT EXISTS lesson_progress (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    lesson_id    BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    completed    BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    UNIQUE (user_id, lesson_id)
);
CREATE INDEX IF NOT EXISTS idx_progress_user ON lesson_progress(user_id);

-- ----------------------------------------------------------------------------
-- SUBMISSIONS (practice answers)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS submissions (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    lesson_id   BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    content     TEXT   NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED'
                CHECK (status IN ('SUBMITTED','PASSED','FAILED')),
    feedback    TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_sub_user_lesson ON submissions(user_id, lesson_id);

-- ----------------------------------------------------------------------------
-- COMMENTS (lesson discussion) & REVIEWS (course rating)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comments (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    lesson_id  BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    parent_id  BIGINT REFERENCES comments(id)         ON DELETE CASCADE,
    body       TEXT   NOT NULL,
    is_hidden  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_comments_lesson ON comments(lesson_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent ON comments(parent_id);

CREATE TABLE IF NOT EXISTS reviews (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    course_id  BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    rating     SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    body       TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, course_id)
);
CREATE INDEX IF NOT EXISTS idx_reviews_course ON reviews(course_id);

-- ----------------------------------------------------------------------------
-- AI MESSAGES (chat history with assistant)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_messages (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id   BIGINT REFERENCES lessons(id) ON DELETE SET NULL,
    role        VARCHAR(20) NOT NULL CHECK (role IN ('USER','ASSISTANT','SYSTEM')),
    content     TEXT   NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_ai_user ON ai_messages(user_id, created_at);

-- ----------------------------------------------------------------------------
-- INTERVIEWS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS interviews (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    started_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at   TIMESTAMPTZ,
    duration_sec  INT,
    score         INT,                       -- 0..100
    warnings      INT NOT NULL DEFAULT 0,    -- anti-cheat warnings
    status        VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS'
                  CHECK (status IN ('IN_PROGRESS','FINISHED','TERMINATED'))
);
CREATE INDEX IF NOT EXISTS idx_interviews_user ON interviews(user_id);

CREATE TABLE IF NOT EXISTS interview_questions (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    title_ru    VARCHAR(200),
    prompt      TEXT NOT NULL,
    prompt_ru   TEXT,
    language    VARCHAR(40) NOT NULL,    -- 'Python','Java','Go'
    difficulty  VARCHAR(20) NOT NULL DEFAULT 'EASY'
                CHECK (difficulty IN ('EASY','MEDIUM','HARD'))
);

CREATE TABLE IF NOT EXISTS interview_answers (
    id            BIGSERIAL PRIMARY KEY,
    interview_id  BIGINT NOT NULL REFERENCES interviews(id)         ON DELETE CASCADE,
    question_id   BIGINT NOT NULL REFERENCES interview_questions(id) ON DELETE CASCADE,
    answer        TEXT NOT NULL DEFAULT '',
    is_correct    BOOLEAN,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (interview_id, question_id)
);

-- ----------------------------------------------------------------------------
-- NOTIFICATIONS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       VARCHAR(40) NOT NULL,        -- COURSE_UPDATE | LESSON_COMPLETE | COMMENT_REPLY
    title      VARCHAR(200) NOT NULL,
    body       TEXT,
    link       TEXT,
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_notif_user_unread ON notifications(user_id, is_read);
