SUMMARY = "Non-proprietary packages from meta-ridgerun"
DESCRIPTION = "Meta-package depending on all meta-ridgerun recipes that do not \
require a RidgeRun proprietary order. Recipes that inherit rr_proprietary, are \
licensed as Proprietary, or fetch from the private customer orders area are \
excluded, because they call bb.parse.SkipRecipe() unless \
RR_CUSTOMER_GITLAB_ORDER_DIR is set. Useful as a build target (e.g. in CI) in \
environments without a configured RidgeRun order."
LICENSE = "MIT"

inherit packagegroup

# Building a packagegroup normally only builds the (empty) packagegroup package
# itself; its RDEPENDS are otherwise pulled in only at image-creation time.
# Recurse do_build into the runtime dependencies so that "bitbake <this>"
# actually compiles every member recipe, which is what we want for CI
# verification.
do_build[recrdeptask] += "do_build"

python __anonymous() {
    import os
    import re
    import glob

    layerdir = d.getVar("LAYERDIR_meta-ridgerun")
    if not layerdir:
        bb.warn("Could not resolve LAYERDIR for meta-ridgerun")
        return

    pn = d.getVar("PN")

    # A recipe is treated as proprietary (and therefore excluded) when it
    # inherits the rr_proprietary class, declares a Proprietary license, or
    # fetches from the private customer orders area. Such recipes call
    # bb.parse.SkipRecipe() unless RR_CUSTOMER_GITLAB_ORDER_DIR is set, so
    # depending on them would break builds that do not have that order.
    proprietary_re = re.compile(
        r'^\s*inherit\b[^\n]*\brr_proprietary\b'
        r'|^\s*LICENSE\s*=\s*["\']\s*Proprietary\s*["\']'
        r'|RR_CUSTOMER_GITLAB_ORDER_DIR',
        re.MULTILINE,
    )

    packages = set()
    for bb_file in glob.glob(os.path.join(layerdir, "recipes-*", "*", "*.bb")):
        recipe = os.path.splitext(os.path.basename(bb_file))[0]
        # Skip self and any other packagegroups: depending on the all-packages
        # group would transitively pull in the proprietary recipes again.
        if recipe == pn or recipe.startswith("packagegroup"):
            continue
        try:
            with open(bb_file, "r") as f:
                content = f.read()
        except OSError as e:
            bb.warn("Could not read %s: %s" % (bb_file, e))
            continue
        if proprietary_re.search(content):
            continue
        packages.add(recipe)

    if packages:
        d.appendVar("RDEPENDS:%s" % pn, " " + " ".join(sorted(packages)))
    else:
        bb.warn("packagegroup-meta-ridgerun-nonproprietary found no non-proprietary recipes to depend on")
}
