-- ========================================
-- LARGE MOCK DATASET (PostgreSQL)
-- Similar style to a typical data.sql sample:
-- - Truncates and resets identities
-- - Generates lots of data with generate_series()
--
-- Usage:
--   psql -d <db> -f src/main/resources/db/seed.sql
-- or set SQL_INIT_MODE=always and configure Spring sql.init.data-locations.
-- ========================================

BEGIN;

-- Clean up existing data
TRUNCATE TABLE
  chat_messages,
  conversations,
  goal_milestones,
  goal_actions,
  user_goals,
  plan_recommendations,
  reviews,
  appointments,
  treatment_plans,
  user_assessment_results,
  assessment_answers,
  assessment_questions,
  assessments,
  post_categories,
  blog_posts,
  blog_categories,
  mind_point_transactions,
  user_subscriptions,
  payments,
  mind_point_purchase_options,
  subscriptions,
  payment_methods,
  meeting_platforms,
  expert_availability,
  expert_specializations,
  expert_languages,
  specializations,
  languages,
  experts,
  users
RESTART IDENTITY CASCADE;

-- ========================================
-- Fix legacy PostgreSQL OID columns (created by @Lob String mappings)
-- NOTE: Spring's SQL initializer splits statements on semicolons and cannot
-- reliably run PL/pgSQL blocks (DO $$...$$). These ALTERs are safe to re-run.
-- ========================================

ALTER TABLE public.subscriptions
  ALTER COLUMN features TYPE text USING features::text;

ALTER TABLE public.reviews
  ALTER COLUMN comment TYPE text USING comment::text;

ALTER TABLE public.plan_recommendations
  ALTER COLUMN recommendation_text TYPE text USING recommendation_text::text;

ALTER TABLE public.goal_actions
  ALTER COLUMN action_text TYPE text USING action_text::text;

ALTER TABLE public.chat_messages
  ALTER COLUMN message_content TYPE text USING message_content::text;

ALTER TABLE public.blog_posts
  ALTER COLUMN content TYPE text USING content::text;

ALTER TABLE public.assessment_questions
  ALTER COLUMN question_text TYPE text USING question_text::text;

ALTER TABLE public.assessment_answers
  ALTER COLUMN answer_text TYPE text USING answer_text::text;

ALTER TABLE public.appointments
  ALTER COLUMN expert_session_notes TYPE text USING expert_session_notes::text;

-- ========================================
-- 1) Reference data
-- ========================================

INSERT INTO languages (lang_code, name) VALUES
  ('vi', 'Vietnamese'),
  ('en', 'English'),
  ('ja', 'Japanese'),
  ('ko', 'Korean'),
  ('fr', 'French')
ON CONFLICT (lang_code) DO NOTHING;

INSERT INTO specializations (name) VALUES
  ('Anxiety'),
  ('Depression'),
  ('Stress Management'),
  ('Relationship'),
  ('Sleep'),
  ('Burnout'),
  ('Self-esteem'),
  ('Trauma'),
  ('Career Coaching'),
  ('Parenting')
ON CONFLICT DO NOTHING;

INSERT INTO meeting_platforms (platform_key, display_name, description, is_active) VALUES
  ('google_meet', 'Google Meet', 'Video call via Google Meet', TRUE),
  ('zoom', 'Zoom', 'Video call via Zoom', TRUE),
  ('ms_teams', 'Microsoft Teams', 'Video call via Teams', TRUE)
ON CONFLICT (platform_key) DO NOTHING;

INSERT INTO payment_methods (method_key, display_name, badge_label, is_active) VALUES
  ('vnpay', 'VNPay', 'VNPay', TRUE),
  ('momo', 'MoMo', 'MoMo', TRUE),
  ('zalopay', 'ZaloPay', 'ZaloPay', TRUE),
  ('card', 'Thẻ tín dụng / Ghi nợ', 'CARD', TRUE),
  ('mindpoints', 'MindPoints', 'POINTS', TRUE),
  ('cash', 'Cash', 'Cash', TRUE)
ON CONFLICT (method_key) DO NOTHING;

INSERT INTO subscriptions (name, price, billing_cycle, tier_subtitle, badge_label, short_desc, features, is_active) VALUES
  ('Basic', 99000, 'MONTHLY', 'Starter', 'BASIC', 'For new users', 'Feature A\nFeature B', TRUE),
  ('Premium', 199000, 'MONTHLY', 'Most Popular', 'PRO', 'More sessions & benefits', 'Feature A\nFeature B\nFeature C', TRUE),
  ('Premium Annual', 1990000, 'YEARLY', 'Best Value', 'PRO+', 'Best price per month', 'Feature A\nFeature B\nFeature C\nFeature D', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO mind_point_purchase_options (points_amount, price_vnd, name, description, badge_label, is_active) VALUES
  (50, 30000, '50 Points', 'Small pack', 'NEW', TRUE),
  (100, 50000, '100 Points', 'Starter pack', 'HOT', TRUE),
  (300, 120000, '300 Points', 'Best value', 'BEST', TRUE),
  (700, 250000, '700 Points', 'Power pack', 'SAVE', TRUE),
  (1500, 500000, '1500 Points', 'Mega pack', 'MEGA', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO blog_categories (name) VALUES
  ('Mindfulness'),
  ('Stress'),
  ('Relationships'),
  ('Sleep'),
  ('Career'),
  ('Wellbeing'),
  ('Habits'),
  ('Parenting')
ON CONFLICT DO NOTHING;

-- Assessments (categories are unique)
INSERT INTO assessments (title, category) VALUES
  ('Anxiety Screening', 'ANXIETY'),
  ('Depression Screening', 'DEPRESSION'),
  ('Stress Check', 'STRESS'),
  ('Sleep Quality', 'SLEEP'),
  ('Burnout Check', 'BURNOUT')
ON CONFLICT (category) DO NOTHING;

-- ========================================
-- 2) Users (200) + Experts (50)
-- ========================================

-- Users: 200
INSERT INTO users (email, password_hash, full_name, phone_number, mindpoints_balance, member_since)
SELECT
  'user' || gs || '@example.com',
  '$2a$10$oqzg73KUPvHypmyASK4TAOgFHq6vOR4xD2lrxmPtvT0dgMfJrnxs6',
  'User ' || gs,
  '09' || LPAD(gs::text, 8, '0'),
  (RANDOM() * 2000)::int,
  (CURRENT_DATE - ((RANDOM() * 900)::int || ' days')::interval)::date
FROM generate_series(1, 200) gs;

-- Experts: 50
INSERT INTO experts (email, password_hash, full_name, title, hourly_rate, is_verified, gender)
SELECT
  'expert' || gs || '@example.com',
  '$2a$10$oqzg73KUPvHypmyASK4TAOgFHq6vOR4xD2lrxmPtvT0dgMfJrnxs6',
  'Expert ' || gs,
  (ARRAY['Clinical Psychologist','Therapist','Coach','Counselor','Psychiatrist'])[1 + (RANDOM() * 4)::int],
  (ARRAY[200000, 250000, 300000, 350000, 450000, 600000])[1 + (RANDOM() * 5)::int],
  (RANDOM() < 0.8),
  (ARRAY['FEMALE','MALE','OTHER'])[1 + (RANDOM() * 2)::int]
FROM generate_series(1, 50) gs;

-- ========================================
-- 3) Expert languages & specializations
-- ========================================

-- Assign languages (each expert speaks ~2 languages on average)
INSERT INTO expert_languages (expert_id, lang_code)
SELECT e.expert_id, l.lang_code
FROM experts e
CROSS JOIN languages l
WHERE (RANDOM() < 0.45)
ON CONFLICT (expert_id, lang_code) DO NOTHING;

-- Ensure at least 1 language per expert
INSERT INTO expert_languages (expert_id, lang_code)
SELECT e.expert_id, 'en'
FROM experts e
WHERE NOT EXISTS (
  SELECT 1 FROM expert_languages el WHERE el.expert_id = e.expert_id
)
ON CONFLICT (expert_id, lang_code) DO NOTHING;

-- Assign specializations (each expert has ~2-3)
INSERT INTO expert_specializations (expert_id, spec_id)
SELECT e.expert_id, s.spec_id
FROM experts e
CROSS JOIN specializations s
WHERE (RANDOM() < 0.25)
ON CONFLICT (expert_id, spec_id) DO NOTHING;

-- Ensure at least 1 specialization per expert
INSERT INTO expert_specializations (expert_id, spec_id)
SELECT e.expert_id, (SELECT spec_id FROM specializations ORDER BY RANDOM() LIMIT 1)
FROM experts e
WHERE NOT EXISTS (
  SELECT 1 FROM expert_specializations es WHERE es.expert_id = e.expert_id
)
ON CONFLICT (expert_id, spec_id) DO NOTHING;

-- ========================================
-- 4) Availability blocks (≈ 50 experts * 14 days * 2 slots = 1400)
-- ========================================

INSERT INTO expert_availability (expert_id, start_time, end_time, is_booked)
SELECT
  e.expert_id,
  (CURRENT_DATE + d.day_offset) + t.start_at,
  (CURRENT_DATE + d.day_offset) + t.end_at,
  FALSE
FROM experts e
CROSS JOIN generate_series(1, 14) AS d(day_offset)
CROSS JOIN (
  VALUES
    (TIME '09:00', TIME '10:00'),
    (TIME '10:30', TIME '11:30')
) AS t(start_at, end_at);

-- ========================================
-- 5) Subscriptions & MindPoint purchases (payments + transactions)
-- ========================================

-- Subscription payments for ~80 users
WITH picked AS (
  SELECT u.user_id, s.sub_id
  FROM users u
  JOIN subscriptions s ON TRUE
  WHERE u.user_id <= 80
  ORDER BY u.user_id, s.sub_id
), paid AS (
  INSERT INTO payments (user_id, amount, status, payment_type, related_id, method_id)
  SELECT
    p.user_id,
    (SELECT price FROM subscriptions WHERE sub_id = p.sub_id),
    'PAID',
    'SUBSCRIPTION',
    p.sub_id,
    (SELECT method_id FROM payment_methods ORDER BY RANDOM() LIMIT 1)
  FROM picked p
  RETURNING payment_id, user_id, related_id
)
INSERT INTO user_subscriptions (user_id, sub_id, payment_id, expiry_date, status)
SELECT
  paid.user_id,
  paid.related_id,
  paid.payment_id,
  (CURRENT_DATE + INTERVAL '30 days')::date,
  'ACTIVE'
FROM paid;

-- MindPoint purchase payments for 120 users + credit transactions
WITH picked AS (
  SELECT u.user_id, mpo.option_id, mpo.points_amount, mpo.price_vnd
  FROM users u
  JOIN mind_point_purchase_options mpo ON TRUE
  WHERE u.user_id <= 120
  ORDER BY u.user_id, mpo.option_id
), chosen AS (
  SELECT DISTINCT ON (user_id)
    user_id, option_id, points_amount, price_vnd
  FROM picked
  ORDER BY user_id, RANDOM()
), pay AS (
  INSERT INTO payments (user_id, amount, status, payment_type, related_id, method_id)
  SELECT
    c.user_id,
    c.price_vnd,
    'PAID',
    'MINDPOINTS',
    c.option_id,
    (SELECT method_id FROM payment_methods ORDER BY RANDOM() LIMIT 1)
  FROM chosen c
  RETURNING payment_id, user_id
)
INSERT INTO mind_point_transactions (user_id, points_amount, reason, related_payment_id)
SELECT
  chosen.user_id,
  chosen.points_amount,
  'PURCHASE',
  pay.payment_id
FROM chosen
JOIN pay ON pay.user_id = chosen.user_id;

-- ========================================
-- 6) Treatment plans (60)
-- ========================================

INSERT INTO treatment_plans (user_id, expert_id, diagnosis_title, status)
SELECT
  u.user_id,
  (SELECT expert_id FROM experts ORDER BY RANDOM() LIMIT 1),
  (ARRAY['Generalized Anxiety','Work Stress','Sleep Issues','Burnout','Low mood'])[1 + (RANDOM() * 4)::int],
  'ACTIVE'
FROM users u
WHERE u.user_id <= 60;

-- Plan recommendations (3 per plan)
INSERT INTO plan_recommendations (plan_id, recommendation_text, is_completed)
SELECT
  tp.plan_id,
  (ARRAY[
    'Practice breathing exercises daily.',
    'Limit caffeine intake after 2pm.',
    'Keep a short daily journal.',
    'Add 20 minutes of walking.',
    'Follow a consistent sleep schedule.'
  ])[1 + (RANDOM() * 4)::int],
  (RANDOM() < 0.35)
FROM treatment_plans tp
CROSS JOIN generate_series(1, 3) gs;

-- User goals (1-2 per plan)
INSERT INTO user_goals (plan_id, title, current_value, target_value, assessment_category)
SELECT
  tp.plan_id,
  (ARRAY['Reduce anxiety score','Improve sleep','Reduce stress level','Increase focus'])[1 + (RANDOM() * 3)::int],
  (ARRAY['8','6','5','4h','5h','3'])[1 + (RANDOM() * 5)::int],
  (ARRAY['3','2','7h','6h','1'])[1 + (RANDOM() * 4)::int],
  (ARRAY['ANXIETY','DEPRESSION','STRESS','SLEEP','BURNOUT'])[1 + (RANDOM() * 4)::int]
FROM treatment_plans tp
WHERE (RANDOM() < 0.8)
UNION ALL
SELECT
  tp.plan_id,
  'Build healthy habit',
  '0',
  '1',
  (ARRAY['STRESS','SLEEP','BURNOUT'])[1 + (RANDOM() * 2)::int]
FROM treatment_plans tp
WHERE (RANDOM() < 0.35);

-- Goal actions (2 per goal)
INSERT INTO goal_actions (goal_id, action_text, is_completed)
SELECT
  g.goal_id,
  (ARRAY[
    '10-minute mindfulness daily',
    'Evening journaling',
    'No screens 1 hour before bed',
    'Weekly reflection',
    'Short breathing break at work'
  ])[1 + (RANDOM() * 4)::int],
  (RANDOM() < 0.3)
FROM user_goals g
CROSS JOIN generate_series(1, 2) gs;

-- Goal milestones (0-1 per goal)
INSERT INTO goal_milestones (goal_id, milestone_text, achieved_date)
SELECT
  g.goal_id,
  'Milestone achieved',
  (CURRENT_DATE - ((RANDOM() * 30)::int || ' days')::interval)::date
FROM user_goals g
WHERE (RANDOM() < 0.35);

-- ========================================
-- 7) Blog posts (100) + categories
-- ========================================

INSERT INTO blog_posts (title, content, author_expert_id)
SELECT
  'Post #' || gs || ' - ' || (ARRAY['Mindfulness','Stress','Sleep','Habits','Career'])[1 + (RANDOM() * 4)::int],
  'This is mock content for post #' || gs || '. ' || repeat('Lorem ipsum ', 10),
  (SELECT expert_id FROM experts ORDER BY RANDOM() LIMIT 1)
FROM generate_series(1, 100) gs;

-- Each post has 1-2 categories
INSERT INTO post_categories (post_id, category_id)
SELECT p.post_id, c.category_id
FROM blog_posts p
JOIN LATERAL (
  SELECT category_id
  FROM blog_categories
  ORDER BY RANDOM()
  LIMIT (CASE WHEN RANDOM() < 0.7 THEN 1 ELSE 2 END)
) c ON TRUE
ON CONFLICT (post_id, category_id) DO NOTHING;

-- ========================================
-- 8) Assessments: questions (8 each) & answers (4 each)
-- ========================================

INSERT INTO assessment_questions (assessment_id, question_text)
SELECT
  a.assessment_id,
  'Question ' || q.qn || ' for ' || a.category
FROM assessments a
CROSS JOIN generate_series(1, 8) AS q(qn);

INSERT INTO assessment_answers (question_id, answer_text, score_value)
SELECT
  q.question_id,
  ans.answer_text,
  ans.score_value
FROM assessment_questions q
CROSS JOIN (
  VALUES
    ('Not at all', 0),
    ('Several days', 1),
    ('More than half the days', 2),
    ('Nearly every day', 3)
) AS ans(answer_text, score_value);

-- User assessment results for 150 users (random category and score)
INSERT INTO user_assessment_results (user_id, assessment_id, total_score, category)
SELECT
  u.user_id,
  a.assessment_id,
  (RANDOM() * 24)::int,
  a.category
FROM users u
JOIN LATERAL (
  SELECT assessment_id, category
  FROM assessments
  ORDER BY RANDOM()
  LIMIT 1
) a ON TRUE
WHERE u.user_id <= 150;

-- ========================================
-- 9) Appointments (300) from availability + payments + transactions + reviews
-- ========================================

-- Pick 300 availability slots and mark as booked
WITH picked AS (
  SELECT availability_id
  FROM expert_availability
  WHERE is_booked = FALSE
  ORDER BY RANDOM()
  LIMIT 300
), upd AS (
  UPDATE expert_availability ea
  SET is_booked = TRUE
  WHERE ea.availability_id IN (SELECT availability_id FROM picked)
  RETURNING ea.availability_id, ea.expert_id, ea.start_time
)
INSERT INTO appointments (
  user_id,
  expert_id,
  availability_id,
  start_time,
  status,
  service_type,
  payment_amount_points,
  expert_session_notes,
  user_subscription_id,
  treatment_plan_id,
  platform_id
)
SELECT
  (1 + (RANDOM() * 199)::int) AS user_id,
  upd.expert_id,
  upd.availability_id,
  upd.start_time,
  (ARRAY['PENDING','CONFIRMED','COMPLETED','CANCELLED'])[1 + (RANDOM() * 3)::int],
  (ARRAY['VIDEO','CHAT','VOICE'])[1 + (RANDOM() * 2)::int],
  (ARRAY[0, 0, 50, 100, 150])[1 + (RANDOM() * 4)::int],
  'Mock session notes. ' || repeat('Note ', 10),
  (SELECT us.user_sub_id FROM user_subscriptions us WHERE us.user_id = (1 + (RANDOM() * 199)::int) ORDER BY RANDOM() LIMIT 1),
  (SELECT tp.plan_id FROM treatment_plans tp WHERE tp.user_id = (1 + (RANDOM() * 199)::int) ORDER BY RANDOM() LIMIT 1),
  (SELECT platform_id FROM meeting_platforms ORDER BY RANDOM() LIMIT 1)
FROM upd;

-- Create appointment payments and link back to appointments
WITH ap AS (
  SELECT appt_id, user_id
  FROM appointments
), pay AS (
  INSERT INTO payments (user_id, amount, status, payment_type, related_id, method_id)
  SELECT
    ap.user_id,
    (ARRAY[200000, 250000, 300000, 350000, 450000])[1 + (RANDOM() * 4)::int],
    'PAID',
    'APPOINTMENT',
    ap.appt_id,
    (SELECT method_id FROM payment_methods ORDER BY RANDOM() LIMIT 1)
  FROM ap
  RETURNING payment_id, related_id
)
UPDATE appointments a
SET payment_id = pay.payment_id
FROM pay
WHERE a.appt_id = pay.related_id;

-- Points transactions for appointments that used points
INSERT INTO mind_point_transactions (user_id, points_amount, reason, related_payment_id)
SELECT
  a.user_id,
  -a.payment_amount_points,
  'APPOINTMENT_PAYMENT',
  a.payment_id
FROM appointments a
WHERE a.payment_amount_points IS NOT NULL AND a.payment_amount_points > 0 AND a.payment_id IS NOT NULL;

-- Reviews for completed appointments (~70%)
INSERT INTO reviews (appt_id, user_id, expert_id, rating, comment)
SELECT
  a.appt_id,
  a.user_id,
  a.expert_id,
  (3 + (RANDOM() * 2)::int),
  (ARRAY[
    'Very helpful session.',
    'Great insights, will continue.',
    'Good experience overall.',
    'Clear guidance and supportive.',
    'Helpful but could be better.'
  ])[1 + (RANDOM() * 4)::int]
FROM appointments a
WHERE a.status = 'COMPLETED' AND RANDOM() < 0.7;

-- ========================================
-- 10) Conversations (80) + messages (5 each = 400)
-- ========================================

INSERT INTO conversations (user_id, expert_id, last_message_at)
SELECT
  (1 + (RANDOM() * 199)::int),
  (1 + (RANDOM() * 49)::int),
  (NOW() - ((RANDOM() * 10)::int || ' hours')::interval)
FROM generate_series(1, 80) gs;

INSERT INTO chat_messages (conversation_id, sender_id, sender_type, message_content, sent_at)
SELECT
  c.conversation_id,
  CASE WHEN (m.idx % 2) = 0 THEN c.user_id ELSE c.expert_id END,
  CASE WHEN (m.idx % 2) = 0 THEN 'USER' ELSE 'EXPERT' END,
  CASE WHEN (m.idx % 2) = 0
    THEN 'User message #' || m.idx
    ELSE 'Expert reply #' || m.idx
  END,
  (c.last_message_at - ((5 - m.idx) || ' minutes')::interval)
FROM conversations c
CROSS JOIN generate_series(1, 5) AS m(idx);

COMMIT;
