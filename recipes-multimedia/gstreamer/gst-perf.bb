SUMMARY = "GStreamer Perf"
DESCRIPTION = "GStreamer element to measure fps and performance"
HOMEPAGE = "https://github.com/RidgeRun/gst-perf-autotools"
SECTION = "multimedia"
LICENSE = "LGPLv2"
LIC_FILES_CHKSUM="file://LICENSE;md5=5f30f0716dfdd0d91eb439ebec522ec2"

DEPENDS = "gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-bad"

SRCBRANCH ?= "master"
SRCREV = "${AUTOREV}"
SRC_URI = "git://github.com/RidgeRun/gst-perf.git;protocol=https;branch=${SRCBRANCH}"

S = "${WORKDIR}/git"

PACKAGECONFIG ??= ""
PACKAGECONFIG[gtk-doc] = "--enable-gtk-doc,,gtk-doc-native"

INSANE_SKIP:${PN} = "dev-so"
FILES:${PN} += "${libdir}/gstreamer-1.0/libgstperf.so \
                ${libdir}/gstreamer-1.0/libgstperf.so.0 \
                ${libdir}/gstreamer-1.0/libgstperf.so.0.0.0 \
               "

inherit autotools pkgconfig gettext

do_configure() {
${S}/autogen.sh
oe_runconf
}

do_install:append() {
	rm -f ${D}${libdir}/gstreamer-1.0/libgstperf.a
}
