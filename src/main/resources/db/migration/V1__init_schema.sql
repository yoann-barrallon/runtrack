CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE training_plans (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                title VARCHAR(150) NOT NULL,
                                goal_distance_type VARCHAR(50), -- 'FIVE_K', 'TEN_K', 'HALF_MARATHON', 'MARATHON'
                                start_date DATE NOT NULL,
                                end_date DATE NOT NULL,
                                status VARCHAR(30) DEFAULT 'ACTIVE' NOT NULL, -- 'ACTIVE', 'COMPLETED', 'ARCHIVED'
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE planned_sessions (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  plan_id UUID NOT NULL REFERENCES training_plans(id) ON DELETE CASCADE,
                                  target_date DATE NOT NULL,
                                  session_type VARCHAR(50) NOT NULL, -- 'ENDURANCE_FONDAMENTALE', 'FRACTIONNE', 'SORTIE_LONGUE'
                                  target_distance_meters INT,
                                  target_duration_seconds INT,
                                  description TEXT,
                                  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE run_sessions (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              planned_session_id UUID REFERENCES planned_sessions(id) ON DELETE SET NULL,
                              title VARCHAR(150) NOT NULL,
                              start_time TIMESTAMP WITH TIME ZONE NOT NULL,
                              duration_seconds INT NOT NULL,
                              distance_meters INT NOT NULL,
                              elevation_gain_meters INT DEFAULT 0,
                              avg_pace_seconds_per_km INT NOT NULL,
                              avg_heart_rate INT,
                              source_type VARCHAR(30) DEFAULT 'MANUAL' NOT NULL, -- 'MANUAL', 'GPX', 'FIT'
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE run_splits (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            run_session_id UUID NOT NULL REFERENCES run_sessions(id) ON DELETE CASCADE,
                            split_number INT NOT NULL,
                            duration_seconds INT NOT NULL,
                            avg_pace_seconds_per_km INT NOT NULL,
                            elevation_gain_meters INT DEFAULT 0
);


CREATE TABLE personal_records (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                  run_session_id UUID REFERENCES run_sessions(id) ON DELETE SET NULL,
                                  distance_type VARCHAR(50) NOT NULL, -- 'ONE_KM', 'FIVE_KM', 'TEN_KM', 'HALF_MARATHON', 'MARATHON'
                                  time_seconds INT NOT NULL,
                                  achieved_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                  CONSTRAINT uq_user_distance_record UNIQUE (user_id, distance_type)
);

CREATE INDEX idx_run_sessions_user_date ON run_sessions(user_id, start_time DESC);
CREATE INDEX idx_planned_sessions_date ON planned_sessions(plan_id, target_date);
CREATE INDEX idx_run_splits_session ON run_splits(run_session_id);