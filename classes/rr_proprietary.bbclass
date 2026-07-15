# Common behavior for RidgeRun proprietary recipes.
#
# Recipes that inherit this class fetch sources from a customer-specific
# GitLab "orders" directory, e.g.
#   git://git@gitlab.ridgerun.com/ridgerun/orders/${RR_CUSTOMER_GITLAB_ORDER_DIR}/<name>.git
#
# Failure modes handled at parse time:
#   1. RR_CUSTOMER_GITLAB_ORDER_DIR is unset -> recipe is skipped cleanly with
#      no network access at all.
#   2. Order is set but the customer's key can't reach this repo (typically
#      because the plugin isn't part of their order) -> the FetchError from
#      the parse-time SRCREV resolution is caught and re-raised as a
#      SkipRecipe with a purchase-oriented message. Parsing does not abort.
#
# AUTOREV keeps working normally: get_srcrev() runs once here and its result
# is cached by the fetcher, so bitbake's own hash machinery reuses it without
# a second network call. Recipes can keep using SRCREV = "${AUTOREV}".

RR_CUSTOMER_GITLAB_ORDER_DIR ??= ""
RR_PURCHASES_URL ??= "https://www.ridgerun.com"

# Keep proprietary recipes out of 'bitbake world' / 'bitbake universe' runs.
# They require customer credentials that generic world builds won't have.
EXCLUDE_FROM_WORLD = "1"

python () {
    pn = d.getVar("PN")
    url = d.getVar("RR_PURCHASES_URL")
    order = d.getVar("RR_CUSTOMER_GITLAB_ORDER_DIR")

    if not order:
        raise bb.parse.SkipRecipe(
            f"{pn} is a RidgeRun proprietary plugin. Set "
            f"RR_CUSTOMER_GITLAB_ORDER_DIR to your customer GitLab order "
            f"directory to enable it. See {url} for purchase details."
        )

    # Proactively resolve SRCREV. If the fetcher can't reach the repo, turn
    # the FetchError into a clean SkipRecipe so unpurchased plugins simply
    # disappear instead of aborting the whole parse.
    try:
        bb.fetch2.get_srcrev(d)
    except bb.fetch2.FetchError as e:
        raise bb.parse.SkipRecipe(
            f"{pn} is a RidgeRun proprietary plugin that is not accessible "
            f"with the current GitLab credentials for order '{order}'. This "
            f"usually means the plugin is not part of your order. See {url} "
            f"for purchase details.\nUnderlying error: {e}"
        )
    except bb.fetch2.NoChecksumError:
        # Not an access issue - let bitbake handle it its normal way.
        pass
}
