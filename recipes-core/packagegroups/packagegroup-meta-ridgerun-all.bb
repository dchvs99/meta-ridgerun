SUMMARY = "Packagegroup covering every recipe in meta-ridgerun"
LICENSE = "MIT"

inherit packagegroup

PACKAGES = "${PN}"

python __anonymous() {
    import os
    import sys
    layer_dir = d.getVar("LAYERDIR_META_RIDGERUN") or d.getVar("FILE_DIRNAME")
    if layer_dir:
        lib_dir = os.path.join(layer_dir, "lib")
        if lib_dir not in sys.path:
            sys.path.insert(0, lib_dir)
    from metaridgerun.recipe_discovery import discover_recipes

    own_pn = d.getVar("PN")
    recipes = discover_recipes(layer_dir, exclude={own_pn})
    d.setVar("RDEPENDS:%s" % own_pn, " ".join(recipes))
}
