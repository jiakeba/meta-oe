require ${BPN}.inc

SRC_URI = "\
    ${E_RELEASES}/libs/${SRCNAME}/${SRCNAME}-${SRCVER}.tar.gz \
    file://0001-configure.ac-Don-t-check-for-Xprint-extension.patch \
    file://0002-evas_3d-Add-Eet.h-includes.patch \
"

SRC_URI[md5sum] = "b0813fe92cb6eaf861e3c179a19d8e41"
SRC_URI[sha256sum] = "30a8a239449e0d068a39787b5a4769dd26ddf0476f58f094c85e65b108086269"

# Temporary disable until error like the one following are fixed
# efl-native/2_1.8.4-r0/efl-1.8.4/src/lib/eet/.libs/libeet.so: file not recognized: File truncated
# collect2: error: ld returned 1 exit status
# x86_64-linux-libtool: install: error: relink `modules/evas/savers/tiff/module.la' with the above command before installing it
# Makefile:11031: recipe for target 'install-savertiffpkgLTLIBRARIES' failed
PARALLEL_MAKE_class-native = ""

#EFL_RUN_IN_TREE=1 ../src/bin/eolian/eolian_gen --eo --legacy -I/OE/build/shr-core/tmp-eglibc/work/x86_64-linux/efl-native/2_1.10.0-r0/efl-1.10.0/src/lib/eo -I/OE/build/shr-core/tmp-eglibc/work/x86_64-linux/efl-native/2_1.10.0-r0/efl-1.10.0/src/lib/evas/canvas -I/OE/build/shr-core/tmp-eglibc/work/x86_64-linux/efl-native/2_1.10.0-r0/efl-1.10.0/src/lib/edje -I/OE/build/shr-core/tmp-eglibc/work/x86_64-linux/efl-native/2_1.10.0-r0/efl-1.10.0/src/lib/ecore_audio --gc -o lib/evas/canvas/evas_line.eo.c /OE/build/shr-core/tmp-eglibc/work/x86_64-linux/efl-native/2_1.10.0-r0/efl-1.10.0/src/lib/evas/canvas/evas_line.eo
#ERR<32439>:eolian_gen /OE/build/shr-core/tmp-eglibc/work/x86_64-linux/efl-native/2_1.10.0-r0/efl-1.10.0/src/bin/eolian/main.c:103 _generate_c_file() Couldnt open file lib/evas/canvas/evas_line.eo.c for writing
#Makefile:30738: recipe for target 'lib/evas/canvas/evas_line.eo.c' failed
#make[2]: *** [lib/evas/canvas/evas_line.eo.c] Error 1
#Makefile:2163: recipe for target 'all-recursive' failed
# autotools-brokensep
B = "${S}"
