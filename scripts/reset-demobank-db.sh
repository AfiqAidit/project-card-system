#!/usr/bin/env bash
# Remove local H2 files when schema is stuck (e.g. missing version column).
set -euo pipefail
rm -rf "${HOME}/.demobank"
echo "Removed ${HOME}/.demobank"
echo "Next: ./scripts/dev.sh redeploy"
