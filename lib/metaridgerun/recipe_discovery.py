"""Recipe discovery for the meta-ridgerun layer.

This module is BitBake-free: it imports nothing from BitBake or OpenEmbedded
and may be imported and exercised by plain CPython (unit tests, ad-hoc shell
invocation). The ``packagegroup-meta-ridgerun-all`` recipe delegates its
enumeration logic here so the same code path is testable outside BitBake.

Walks a layer root directory and returns the sorted, deduplicated list of
recipe names (``PN``) by listing every file under any ``recipes-*/`` subtree
that ends in ``.bb`` and stripping the trailing ``.bb`` suffix and any
trailing ``_<version>`` suffix whose ``<version>`` starts with an ASCII digit.

Only plain string operations (``str.endswith``, ``str.rpartition``,
``str.isdigit``) are used. Regular expressions are intentionally avoided.
"""

from __future__ import annotations

import logging
import os
from typing import Iterable

__all__ = ["discover_recipes"]


def _strip_suffixes(filename: str) -> str:
    """Derive the recipe name (PN) from a ``.bb`` filename.

    Examples::

        _strip_suffixes("gstd.bb")       -> "gstd"
        _strip_suffixes("gstd_1.0.0.bb") -> "gstd"
        _strip_suffixes("foo_bar.bb")    -> "foo_bar"

    Raises ``ValueError`` if ``filename`` does not end in ``.bb``.
    """
    if not filename.endswith(".bb"):
        raise ValueError(f"not a .bb filename: {filename}")
    stem = filename[:-3]  # drop the trailing ".bb"
    head, sep, tail = stem.rpartition("_")
    if sep and tail and tail[0].isdigit():
        return head
    return stem


def discover_recipes(layer_dir: str, exclude: Iterable[str] = ()) -> list[str]:
    """Return the sorted deduplicated set of PN values under ``layer_dir``.

    Walks every ``recipes-*/`` subdirectory under ``layer_dir`` and collects
    the basename of every file ending in ``.bb``. ``.bbappend`` files and
    any filename that does not end in ``.bb`` are skipped. Names listed in
    ``exclude`` are dropped from the final set.

    When the discovered set is empty a single warning is emitted via the
    standard library ``logging`` module and ``[]`` is returned.
    """
    layer_dir = os.path.abspath(layer_dir)
    excluded = set(exclude)
    discovered: set[str] = set()

    for entry in sorted(os.listdir(layer_dir)):
        if not entry.startswith("recipes-"):
            continue
        subtree = os.path.join(layer_dir, entry)
        if not os.path.isdir(subtree):
            continue
        for root, _dirs, files in os.walk(subtree):
            for name in files:
                if not name.endswith(".bb"):
                    # skips .bbappend and anything else
                    continue
                pn = _strip_suffixes(name)
                if pn and pn not in excluded:
                    discovered.add(pn)

    if not discovered:
        logging.getLogger(__name__).warning(
            "no .bb files discovered under %s", layer_dir
        )
        return []

    return sorted(discovered)
