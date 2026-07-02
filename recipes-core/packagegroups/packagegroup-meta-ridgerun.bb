SUMMARY = "All packages from meta-ridgerun"
LICENSE = "MIT"

inherit packagegroup

python __anonymous() {
    import os
    import glob

    layerdir = d.getVar("LAYERDIR_meta-ridgerun")
    if not layerdir:
        bb.warn("Could not resolve LAYERDIR for meta-ridgerun")
        return

    pn = d.getVar("PN")
    packages = set()

    for bb_file in glob.glob(os.path.join(layerdir, "recipes-*", "*", "*.bb")):
        recipe = os.path.splitext(os.path.basename(bb_file))[0]
        if recipe == pn:
            continue
        packages.add(recipe)

    if packages:
        d.appendVar("RDEPENDS:%s" % pn, " " + " ".join(sorted(packages)))
}
