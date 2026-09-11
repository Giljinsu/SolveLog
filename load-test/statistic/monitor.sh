#!/bin/bash
# 부하테스트 중 DB/JVM 리소스를 2초 간격으로 샘플링해서 CSV로 저장한다.
# 사용법: ./monitor.sh <output_csv> <duration_seconds>

OUT_FILE="$1"
DURATION="$2"
PGPASSWORD=solve1234
export PGPASSWORD

JAVA_PID=$(lsof -ti :8080 -sTCP:LISTEN | head -1)

echo "timestamp,pg_active_conn,pg_idle_conn,pg_total_conn,java_cpu_pct,java_rss_mb,redis_connected_clients" > "$OUT_FILE"

END=$((SECONDS + DURATION))
while [ $SECONDS -lt $END ]; do
  TS=$(date +%H:%M:%S)

  PG_STATS=$(psql -h localhost -U solvelog_admin -d solvelog -t -A -F"," -c "
    select
      count(*) filter (where state = 'active'),
      count(*) filter (where state = 'idle'),
      count(*)
    from pg_stat_activity
    where datname = 'solvelog';
  " 2>/dev/null)

  if [ -n "$JAVA_PID" ]; then
    JAVA_STATS=$(ps -o %cpu=,rss= -p "$JAVA_PID" 2>/dev/null | awk '{printf "%s,%.1f", $1, $2/1024}')
  else
    JAVA_STATS="NA,NA"
  fi

  REDIS_CLIENTS=$(redis-cli info clients 2>/dev/null | grep connected_clients | tr -d '\r' | cut -d: -f2)

  echo "$TS,$PG_STATS,$JAVA_STATS,$REDIS_CLIENTS" >> "$OUT_FILE"
  sleep 2
done
