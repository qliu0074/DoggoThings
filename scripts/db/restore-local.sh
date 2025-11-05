#!/usr/bin/env bash
# English: restore local dump. Placeholder.
set -euo pipefail
DB=${DB:-nail}
psql -h localhost -U postgres -d "$DB" < dump.sql
