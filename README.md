meta-ridgerun
=============

RidgeRun vendor layer for Yocto/OE, kept minimal and ready for project-specific recipes and bbappends.

Compatibility
-------------
- Yocto release: scarthgap
- LAYERSERIES_COMPAT: meta-ridgerun = "scarthgap"

Quick Start
-----------
Start the Yocto container with this checkout mounted, then initialize the build
from builder's published source lock:
```bash
cd /workdir/meta-ridgerun

YOCTO_RELEASE=scarthgap
META_RIDGERUN_DIR="$PWD"
WORKSPACE="$PWD/build"
SETUP_DIR_NAME="meta-ridgerun-${YOCTO_RELEASE}"
BUILD_DIR="$WORKSPACE/yocto/$SETUP_DIR_NAME/build"
SOURCE_LOCK_URL="https://yocto.ridgerun.com/mirror/${YOCTO_RELEASE}/source-locks/sources-fixed-revisions_${YOCTO_RELEASE}_latest.json"

mkdir -p "$WORKSPACE"
git clone git@gitlab.ridgerun.com:tex/yocto/builder.git -b "$YOCTO_RELEASE" "$WORKSPACE/builder"

bitbake-setup -q \
    --setting default top-dir-prefix "$WORKSPACE" \
    --setting default top-dir-name yocto \
    init \
    --non-interactive \
    --no-init-vscode \
    --setup-dir-name "$SETUP_DIR_NAME" \
    --source-overrides <(curl -fsS "$SOURCE_LOCK_URL") \
    "$WORKSPACE/builder/bitbake/config/${YOCTO_RELEASE}.conf.json" default

echo "require $WORKSPACE/builder/build/conf/local.conf" >> "$BUILD_DIR/conf/local.conf"
cp build/conf/auto.conf "$BUILD_DIR/conf/auto.conf"

source "$BUILD_DIR/init-build-env"
bitbake-layers add-layer "$META_RIDGERUN_DIR"
```

This will:
1. Clone the RidgeRun `builder` repo under `build/builder`
2. Replay the latest `sources-fixed-revisions` lock from the sstate mirror
3. Reuse builder's BitBake configuration
4. Enable the sstate mirror through `build/conf/auto.conf`
5. Add this checkout as the `meta-ridgerun` layer under test

After setup completes, build any meta-ridgerun recipe:
```bash
MACHINE=jetson-agx-orin-devkit bitbake gst-perf
```

Or build all of them at once:
```bash
MACHINE=jetson-agx-orin-devkit bitbake packagegroup-meta-ridgerun-all
```

Contents
--------
- `conf/layer.conf` — layer metadata and compatibility settings
- `classes/rr_proprietary.bbclass` — class for proprietary RidgeRun recipes
- `recipes-multimedia/gstreamer/` — GStreamer plugin recipes (gst-interpipes, gst-shark, gstd, etc.)
- `recipes-core/packagegroups/` — packagegroup that pulls in every recipe in the layer
- `lib/metaridgerun/` — Python helpers used by the packagegroup
- `build/conf/auto.conf` — enables the sstate mirror policy imported from builder

Using the layer in an existing build
------------------------------------
From your build directory (e.g. `build`):
```
bitbake-layers add-layer ../meta-ridgerun
```
If you maintain `bblayers.conf` manually, append:
```
BBLAYERS += "${TOPDIR}/../meta-ridgerun"
```

Customer repositories
---------------------
Proprietary plugins in this layer fetch from private RidgeRun GitLab paths under `git@gitlab.ridgerun.com/ridgerun/orders/${RR_CUSTOMER_GITLAB_ORDER_DIR}`. Set the directory you were given in one of these ways (prefer `conf/local.conf`):
- In `conf/local.conf`: `RR_CUSTOMER_GITLAB_ORDER_DIR = "customer-1234"`
- From the environment (Yocto Scarthgap): `export RR_CUSTOMER_GITLAB_ORDER_DIR=customer-1234` and `export BB_ENV_PASSTHROUGH_ADDITIONS="$BB_ENV_PASSTHROUGH_ADDITIONS RR_CUSTOMER_GITLAB_ORDER_DIR"` before running BitBake

Support and contributions
-------------------------
For changes and questions, reach out to the RidgeRun Yocto maintenance team.
RidgeRun website: https://www.ridgerun.com/
Support email: support@ridgerun.com

CI
--
A GitHub Actions pipeline builds every recipe in this layer through
`packagegroup-meta-ridgerun-all`. It clones `builder`, fetches the latest
`sources-fixed-revisions_${YOCTO_RELEASE}_latest.json` from
`https://yocto.ridgerun.com/mirror`, runs `bitbake-setup -q`, imports builder's
reference BitBake configuration, and adds this checkout as the layer under test.

Required secrets:
- `DEPLOY_USER` — GitLab user/token name with read access to `builder`.
- `DEPLOY_TOKEN` — Token paired with `DEPLOY_USER`.

The pipeline runs on every commit, every Sunday at 3 AM Costa Rica time, manual
dispatch, and `repository_dispatch` events from the upstream cache builder.

Triggers: `push`, `schedule`, `workflow_dispatch`, `repository_dispatch`.
