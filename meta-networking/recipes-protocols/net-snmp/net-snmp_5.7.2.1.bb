SUMMARY = "Various tools relating to the Simple Network Management Protocol"
HOMEPAGE = "http://www.net-snmp.org/"
LICENSE = "BSD"

LIC_FILES_CHKSUM = "file://README;beginline=3;endline=8;md5=7f7f00ba639ac8e8deb5a622ea24634e"

DEPENDS = "openssl libnl pciutils"

SRC_URI = "${SOURCEFORGE_MIRROR}/net-snmp/net-snmp-${PV}.zip \
        file://0001-Added-checks-for-printing-variables-with-wrong-types.patch \
        file://init \
        file://snmpd.conf \
        file://snmptrapd.conf \
        file://systemd-support.patch \
        file://snmpd.service \
        file://snmptrapd.service \
        file://ifmib.patch \
        file://net-snmp-add-knob-whether-nlist.h-are-checked.patch \
        file://fix-libtool-finish.patch \
        file://net-snmp-testing-add-the-output-format-for-ptest.patch \
        file://run-ptest \
        file://0001-Fix-CVE-2014-2285.patch \
"

SRC_URI[md5sum] = "a2c83518648b0f2a5d378625e45c0e18"
SRC_URI[sha256sum] = "ac9105539971f7cfb1456a86d479e18e8a8b3712212595ad40504347ba5843da"

inherit autotools update-rc.d siteinfo systemd

EXTRA_OEMAKE = "INSTALL_PREFIX=${D}"

PARALLEL_MAKE = ""
CCACHE = ""

TARGET_CC_ARCH += "${LDFLAGS}"

PACKAGECONFIG ??= ""
PACKAGECONFIG[elfutils] = "--with-elf, --without-elf, elfutils"

EXTRA_OECONF = "--disable-embedded-perl \
                --with-perl-modules=no \
                --enable-shared \
                --disable-manuals \
                --with-defaults \
                --with-persistent-directory=${localstatedir}/lib/net-snmp \
                ${@base_conditional('SITEINFO_ENDIANNESS', 'le', '--with-endianness=little', '--with-endianness=big', d)}"

# net-snmp needs to have mib-modules=smux enabled to enable quagga to support snmp
EXTRA_OECONF += "--with-mib-modules=smux"

CACHED_CONFIGUREVARS = " \
    ac_cv_header_valgrind_valgrind_h=no \
    ac_cv_header_valgrind_memcheck_h=no \
"

do_configure_prepend() {
    export PERLPROG="${bindir}/env perl"
}

do_install_append() {
    install -d ${D}${sysconfdir}/snmp
    install -d ${D}${sysconfdir}/init.d
    install -m 755 ${WORKDIR}/init ${D}${sysconfdir}/init.d/snmpd
    install -m 644 ${WORKDIR}/snmpd.conf ${D}${sysconfdir}/snmp/
    install -m 644 ${WORKDIR}/snmptrapd.conf ${D}${sysconfdir}/snmp/
    sed -e "s@-I/usr/include@@g" \
        -e "s@^prefix=.*@prefix=${STAGING_DIR_HOST}@g" \
        -e "s@^exec_prefix=.*@exec_prefix=${STAGING_DIR_HOST}@g" \
        -e "s@^includedir=.*@includedir=${STAGING_INCDIR}@g" \
        -e "s@^libdir=.*@libdir=${STAGING_LIBDIR}@g" \
        -i ${D}${bindir}/net-snmp-config
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/snmpd.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/snmptrapd.service ${D}${systemd_unitdir}/system
}

do_install_ptest() {
    install -d ${D}${PTEST_PATH}
    for i in ${S}/dist ${S}/include ${B}/include ${S}/mibs ${S}/configure \
        ${B}/net-snmp-config ${S}/testing; do
        if [ -e "$i" ]; then
            cp -a "$i" ${D}${PTEST_PATH}
        fi
    done
    echo `autoconf -V|awk '/autoconf/{print $NF}'` > ${D}${PTEST_PATH}/dist/autoconf-version

    rmdlist="${D}${PTEST_PATH}/dist/net-snmp-solaris-build"
    for i in $rmdlist; do
        if [ -d "$i" ]; then
            rm -rf "$i"
        fi
    done
}

SYSROOT_PREPROCESS_FUNCS += "net_snmp_sysroot_preprocess"

net_snmp_sysroot_preprocess () {
    if [ -e ${D}${bindir}/net-snmp-config ]; then
        install -d ${SYSROOT_DESTDIR}${bindir_crossscripts}/
        install -m 755 ${D}${bindir}/net-snmp-config ${SYSROOT_DESTDIR}${bindir_crossscripts}/
    fi
}

PACKAGES += "${PN}-libs ${PN}-mibs ${PN}-server ${PN}-client ${PN}-server-snmpd ${PN}-server-snmptrapd"

ALLOW_EMPTY_${PN} = "1"
ALLOW_EMPTY_${PN}-server = "1"

FILES_${PN}-libs = "${libdir}/lib*${SOLIBS}"
FILES_${PN}-mibs = "${datadir}/snmp/mibs"
FILES_${PN}-server-snmpd = "${sbindir}/snmpd \
                            ${sysconfdir}/snmp/snmpd.conf \
                            ${sysconfdir}/init.d \
                            ${systemd_unitdir}/system/snmpd.service \
"

FILES_${PN}-server-snmptrapd = "${sbindir}/snmptrapd \
                                ${sysconfdir}/snmp/snmptrapd.conf \
                                ${systemd_unitdir}/system/snmptrapd.service \
"

FILES_${PN} = ""
FILES_${PN}-client = "${bindir}/* ${datadir}/snmp/"
FILES_${PN}-dbg += "${libdir}/.debug/ ${sbindir}/.debug/ ${bindir}/.debug/"
FILES_${PN}-dev += "${bindir}/mib2c ${bindir}/mib2c-update"

CONFFILES_${PN}-server-snmpd = "${sysconfdir}/snmp/snmpd.conf"
CONFFILES_${PN}-server-snmptrapd = "${sysconfdir}/snmp/snmptrapd.conf"

INITSCRIPT_PACKAGES = "${PN}-server"
INITSCRIPT_NAME_${PN}-server = "snmpd"
INITSCRIPT_PARAMS_${PN}-server = "start 90 2 3 4 5 . stop 60 0 1 6 ."

EXTRA_OECONF += "${@base_contains('DISTRO_FEATURES', 'systemd', '--with-systemd', '--without-systemd', d)}"

SYSTEMD_PACKAGES = "${PN}-server-snmpd \
                    ${PN}-server-snmptrapd"

SYSTEMD_SERVICE_${PN}-server-snmpd = "snmpd.service"
SYSTEMD_SERVICE_${PN}-server-snmptrapd =  "snmptrapd.service"

RDEPENDS_${PN} += "net-snmp-client"
RDEPENDS_${PN}-server-snmpd += "net-snmp-mibs"
RDEPENDS_${PN}-server-snmptrapd += "net-snmp-server-snmpd"
RDEPENDS_${PN}-server += "net-snmp-server-snmpd net-snmp-server-snmptrapd"
RDEPENDS_${PN}-client += "net-snmp-mibs"
RDEPENDS_${PN}-ptest += "perl \
                         perl-module-test \
                         perl-module-file-basename \
                         perl-module-getopt-long \
                         perl-module-file-temp \
                         perl-module-data-dumper \
"
RDEPENDS_${PN}-dev = "net-snmp-client (= ${EXTENDPKGV}) net-snmp-server (= ${EXTENDPKGV})"
RRECOMMENDS_${PN}-dbg = "net-snmp-client (= ${EXTENDPKGV}) net-snmp-server (= ${EXTENDPKGV})"

RPROVIDES_${PN}-server-snmpd += "${PN}-server-snmpd-systemd"
RREPLACES_${PN}-server-snmpd += "${PN}-server-snmpd-systemd"
RCONFLICTS_${PN}-server-snmpd += "${PN}-server-snmpd-systemd"

RPROVIDES_${PN}-server-snmptrapd += "${PN}-server-snmptrapd-systemd"
RREPLACES_${PN}-server-snmptrapd += "${PN}-server-snmptrapd-systemd"
RCONFLICTS_${PN}-server-snmptrapd += "${PN}-server-snmptrapd-systemd"

LEAD_SONAME = "libnetsnmp.so"
