-- ============================================================================
-- Learnly: Online Learning Platform Schema (PostgreSQL)
-- Fully normalized, with foreign keys and indexes.
-- ============================================================================

-- Drop in dependency order (safe re-run)
DROP TABLE IF EXISTS interview_answers       CASCADE;
DROP TABLE IF EXISTS interview_questions     CASCADE;
DROP TABLE IF EXISTS interviews              CASCADE;
DROP TABLE IF EXISTS ai_messages             CASCADE;
DROP TABLE IF EXISTS notifications           CASCADE;
DROP TABLE IF EXISTS reviews                 CASCADE;
DROP TABLE IF EXISTS comments                CASCADE;
DROP TABLE IF EXISTS submissions             CASCADE;
DROP TABLE IF EXISTS lesson_progress         CASCADE;
DROP TABLE IF EXISTS enrollments             CASCADE;
DROP TABLE IF EXISTS lessons                 CASCADE;
DROP TABLE IF EXISTS courses                 CASCADE;
DROP TABLE IF EXISTS refresh_tokens          CASCADE;
DROP TABLE IF EXISTS password_reset_tokens   CASCADE;
DROP TABLE IF EXISTS email_verification_tokens CASCADE;
DROP TABLE IF EXISTS users                   CASCADE;

-- ----------------------------------------------------------------------------
-- USERS
-- ----------------------------------------------------------------------------
CREATE TABLE users (
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
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);

-- ----------------------------------------------------------------------------
-- AUTH TOKENS
-- ----------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);

CREATE TABLE password_reset_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE email_verification_tokens (
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
CREATE TABLE courses (
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
CREATE INDEX idx_courses_language ON courses(language);

CREATE TABLE lessons (
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
CREATE INDEX idx_lessons_course ON lessons(course_id);

-- ----------------------------------------------------------------------------
-- ENROLLMENTS & PROGRESS
-- ----------------------------------------------------------------------------
CREATE TABLE enrollments (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    course_id   BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, course_id)
);
CREATE INDEX idx_enroll_user   ON enrollments(user_id);
CREATE INDEX idx_enroll_course ON enrollments(course_id);

CREATE TABLE lesson_progress (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    lesson_id    BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    completed    BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    UNIQUE (user_id, lesson_id)
);
CREATE INDEX idx_progress_user ON lesson_progress(user_id);

-- ----------------------------------------------------------------------------
-- SUBMISSIONS (practice answers)
-- ----------------------------------------------------------------------------
CREATE TABLE submissions (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    lesson_id   BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    content     TEXT   NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED'
                CHECK (status IN ('SUBMITTED','PASSED','FAILED')),
    feedback    TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_sub_user_lesson ON submissions(user_id, lesson_id);

-- ----------------------------------------------------------------------------
-- COMMENTS (lesson discussion) & REVIEWS (course rating)
-- ----------------------------------------------------------------------------
CREATE TABLE comments (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    lesson_id  BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    parent_id  BIGINT REFERENCES comments(id)         ON DELETE CASCADE,
    body       TEXT   NOT NULL,
    is_hidden  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_comments_lesson ON comments(lesson_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);

CREATE TABLE reviews (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    course_id  BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    rating     SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    body       TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, course_id)
);
CREATE INDEX idx_reviews_course ON reviews(course_id);

-- ----------------------------------------------------------------------------
-- AI MESSAGES (chat history with assistant)
-- ----------------------------------------------------------------------------
CREATE TABLE ai_messages (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id   BIGINT REFERENCES lessons(id) ON DELETE SET NULL,
    role        VARCHAR(20) NOT NULL CHECK (role IN ('USER','ASSISTANT','SYSTEM')),
    content     TEXT   NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ai_user ON ai_messages(user_id, created_at);

-- ----------------------------------------------------------------------------
-- INTERVIEWS
-- ----------------------------------------------------------------------------
CREATE TABLE interviews (
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
CREATE INDEX idx_interviews_user ON interviews(user_id);

CREATE TABLE interview_questions (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    title_ru    VARCHAR(200),
    prompt      TEXT NOT NULL,
    prompt_ru   TEXT,
    language    VARCHAR(40) NOT NULL,    -- 'Python','Java','Go'
    difficulty  VARCHAR(20) NOT NULL DEFAULT 'EASY'
                CHECK (difficulty IN ('EASY','MEDIUM','HARD'))
);

CREATE TABLE interview_answers (
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
CREATE TABLE notifications (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       VARCHAR(40) NOT NULL,        -- COURSE_UPDATE | LESSON_COMPLETE | COMMENT_REPLY
    title      VARCHAR(200) NOT NULL,
    body       TEXT,
    link       TEXT,
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notif_user_unread ON notifications(user_id, is_read);
