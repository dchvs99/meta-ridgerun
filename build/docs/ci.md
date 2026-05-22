# CI/CD Documentation

The GitHub Actions workflow builds every recipe in `meta-ridgerun` through `packagegroup-meta-ridgerun-all`.

The setup is intentionally shared with the GitLab `builder` project:

1. Clone the private `builder` repository.
2. Read the mirror URL and reference BitBake config from `builder`.
3. Fetch the latest `sources-fixed-revisions_<release>_latest.json` source lock from the mirror.
4. Run `bitbake-setup` with the source lock via `--source-overrides`, using the config from `builder/bitbake/config/`.
5. Add the local GitHub checkout of `meta-ridgerun` as the layer under test.
6. Build `packagegroup-meta-ridgerun-all`.

The generated setup lives under:

```text
/workdir/meta-ridgerun/build/yocto/meta-ridgerun-scarthgap/
  build/
  layers/
  config/
```

## Required Secrets

| Name | Purpose |
| --- | --- |
| `DEPLOY_USER` | GitLab username or deploy-token user with read access to `builder`. |
| `DEPLOY_TOKEN` | Token paired with `DEPLOY_USER`. |

The mirror URL is not stored as a GitHub secret anymore. The bootstrap reads it from the cloned builder reference config at `build/conf/include/default-sstate-cache-policy.inc`.

## Triggers

| Trigger | Purpose |
| --- | --- |
| `push` | Validate every branch update. |
| `schedule` | Weekly cache-correctness run: `0 9 * * 0`. |
| `workflow_dispatch` | Manual run with `machine` and `pin_to_source_lock` inputs. |
| `repository_dispatch` | External event hook named `sstate-cache-refreshed`. |

## Local Use

Run:

```bash
bash build/scripts/yocto-bootstrap.sh
```

By default the script launches the Yocto container, clones `builder`, fetches the latest source lock, initializes the setup through `bitbake-setup`, adds this checkout as a layer, and drops into a BitBake-ready shell.

Useful overrides:

| Variable | Default | Purpose |
| --- | --- | --- |
| `MACHINE` | `jetson-agx-orin-devkit` | Machine used in the final build hint. |
| `RR_BOOTSTRAP_PIN` | `1` | Set to `0` to skip the published source lock and use release branch heads. |
| `WORKSPACE` | `<repo>/build` | Local workspace for builder clone, tool clone, setup, and generated files. |

After bootstrap:

```bash
MACHINE=jetson-agx-orin-devkit bitbake packagegroup-meta-ridgerun-all
```
