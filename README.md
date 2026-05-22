meta-ridgerun
=============

RidgeRun vendor layer for Yocto/OE, kept minimal and ready for project-specific recipes and bbappends.

Compatibility
-------------
- Yocto release: scarthgap
- LAYERSERIES_COMPAT: meta-ridgerun = "scarthgap"

Quick Start
-----------
Bootstrap the build environment:
```bash
bash build/scripts/yocto-bootstrap.sh
```

This will:
1. Launch a Docker container with the Yocto toolchain
2. Clone the RidgeRun `builder` repo
3. Use builder's pinned `bitbake-setup` tool and setup config
4. Replay the latest `sources-fixed-revisions` lock from the sstate mirror
5. Add this checkout as the `meta-ridgerun` layer under test
6. Configure the sstate mirror through `build/conf/auto.conf`

After bootstrap completes, build any meta-ridgerun recipe:
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
- `build/scripts/yocto-bootstrap.sh` — one-shot bootstrap that sets up a full Yocto build env

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
A GitHub Actions pipeline builds every recipe in this layer (via `packagegroup-meta-ridgerun-all`) on every commit. The pipeline also runs every Sunday at 3 AM Costa Rica time, on manual dispatch, and on `repository_dispatch` events from the upstream cache builder.

Triggers: `push`, `schedule`, `workflow_dispatch`, `repository_dispatch`.

For details on secrets, the local-use recipe, and how the bootstrap script works, see [build/docs/ci.md](build/docs/ci.md).
