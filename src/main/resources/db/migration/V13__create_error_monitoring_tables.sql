CREATE TABLE public.error_issue (
    issue_id bigint NOT NULL,
    fingerprint character varying(64) NOT NULL,
    exception_class character varying(255) NOT NULL,
    representative_message character varying(1000),
    severity character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    first_occurred_at timestamp(6) without time zone NOT NULL,
    last_occurred_at timestamp(6) without time zone NOT NULL,
    occurrence_count bigint NOT NULL DEFAULT 1,
    ai_analysis_status character varying(20) NOT NULL,
    ai_summary text,
    ai_possible_cause text,
    ai_impact text,
    ai_solution text,
    ai_check_points text,
    analyzed_at timestamp(6) without time zone,
    created_date timestamp(6) without time zone,
    last_modified_date timestamp(6) without time zone
);

ALTER TABLE public.error_issue OWNER TO solvelog_admin;

CREATE SEQUENCE public.error_issue_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.error_issue_seq OWNER TO solvelog_admin;

ALTER TABLE ONLY public.error_issue
    ADD CONSTRAINT error_issue_pkey PRIMARY KEY (issue_id);

ALTER TABLE ONLY public.error_issue
    ADD CONSTRAINT error_issue_fingerprint_key UNIQUE (fingerprint);


CREATE TABLE public.error_event (
    event_id bigint NOT NULL,
    issue_id bigint NOT NULL,
    occurred_at timestamp(6) without time zone NOT NULL,
    trace_id character varying(36),
    request_uri character varying(500),
    http_method character varying(10),
    http_status integer,
    user_id bigint,
    username character varying(255),
    ip character varying(64),
    stack_trace_excerpt text
);

ALTER TABLE public.error_event OWNER TO solvelog_admin;

CREATE SEQUENCE public.error_event_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.error_event_seq OWNER TO solvelog_admin;

ALTER TABLE ONLY public.error_event
    ADD CONSTRAINT error_event_pkey PRIMARY KEY (event_id);

-- ErrorIssue를 삭제하지 않으므로 ON DELETE CASCADE는 불필요하다.
-- ErrorEvent만 보존 기간(§5 monitoring.error-event-retention-days) 경과 후 정리 배치가 삭제한다.
ALTER TABLE ONLY public.error_event
    ADD CONSTRAINT fk_error_event_issue FOREIGN KEY (issue_id) REFERENCES public.error_issue(issue_id);

CREATE INDEX idx_error_event_issue_id ON public.error_event (issue_id);
CREATE INDEX idx_error_event_occurred_at ON public.error_event (occurred_at);
