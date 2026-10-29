-- Intentionally public credentials for local review; never use them in a deployed service.
-- alice / alice-demo-pass; bob / bob-demo-pass
INSERT INTO app_users (username, password_hash) VALUES
('alice', '$2b$10$bD2VhKj1azvK/zUPvt0Zbuz1rj44XN//H/TDUQta6L6NSzAD25NS.'),
('bob', '$2b$10$aTD.q4nS/jKSsj6EIK70ve7InuwZz0sGakyi0kDV.J.k94YEFmmFW');
