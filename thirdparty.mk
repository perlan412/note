
LOCAL_PATH:= vendor/sprd/open-source/res/thirdparty


aaa_files := $(shell ls $(LOCAL_PATH)/uitechno )
PRODUCT_COPY_FILES += \
 $(foreach file, $(aaa_files), $(LOCAL_PATH)/uitechno/$(file):system/lib/uitechno/$(file)) \

bbb_files := $(shell ls $(LOCAL_PATH)/uitechno/defaulttheme )
PRODUCT_COPY_FILES += \
 $(foreach file, $(bbb_files), $(LOCAL_PATH)/uitechno/defaulttheme/$(file):system/lib/uitechno/defaulttheme/$(file)) \

ccc_files := $(shell ls $(LOCAL_PATH)/uitechno/defaulttheme/icon )
PRODUCT_COPY_FILES += \
 $(foreach file, $(ccc_files), $(LOCAL_PATH)/uitechno/defaulttheme/icon/$(file):system/lib/uitechno/defaulttheme/icon/$(file)) \

kkk_files := $(shell ls $(LOCAL_PATH)/uitechno/defaulttheme/com.yulong.android.launcher3 )
PRODUCT_COPY_FILES += \
 $(foreach file, $(kkk_files), $(LOCAL_PATH)/uitechno/defaulttheme/com.yulong.android.launcher3/$(file):system/lib/uitechno/defaulttheme/com.yulong.android.launcher3/$(file)) \

lll_files := $(shell ls $(LOCAL_PATH)/uitechno/defaulttheme/com.yulong.android.launcher3/res/drawable-xhdpi )
PRODUCT_COPY_FILES += \
 $(foreach file, $(lll_files), $(LOCAL_PATH)/uitechno/defaulttheme/com.yulong.android.launcher3/res/drawable-xhdpi/$(file):system/lib/uitechno/defaulttheme/com.yulong.android.launcher3/res/drawable-xhdpi/$(file)) \

ddd_files := $(shell ls $(LOCAL_PATH)/uitechno/default_wallpaper )
PRODUCT_COPY_FILES += \
 $(foreach file, $(ddd_files), $(LOCAL_PATH)/uitechno/default_wallpaper/$(file):system/lib/uitechno/default_wallpaper/$(file)) \

eee_files := $(shell ls $(LOCAL_PATH)/uitechno/icon )
PRODUCT_COPY_FILES += \
 $(foreach file, $(eee_files), $(LOCAL_PATH)/uitechno/icon/$(file):system/lib/uitechno/icon/$(file)) \

fff_files := $(shell ls $(LOCAL_PATH)/uitechno/keyguard )
PRODUCT_COPY_FILES += \
 $(foreach file, $(fff_files), $(LOCAL_PATH)/uitechno/keyguard/$(file):system/lib/uitechno/keyguard/$(file)) \

ggg_files := $(shell ls $(LOCAL_PATH)/uitechno/primary )
PRODUCT_COPY_FILES += \
 $(foreach file, $(ggg_files), $(LOCAL_PATH)/uitechno/primary/$(file):system/lib/uitechno/primary/$(file)) \

hhh_files := $(shell ls $(LOCAL_PATH)/uitechno/theme )
PRODUCT_COPY_FILES += \
 $(foreach file, $(hhh_files), $(LOCAL_PATH)/uitechno/theme/$(file):system/lib/uitechno/theme/$(file)) \

jjj_files := $(shell ls $(LOCAL_PATH)/uitechno/wallpaper )
PRODUCT_COPY_FILES += \
 $(foreach file, $(jjj_files), $(LOCAL_PATH)/uitechno/wallpaper/$(file):system/lib/uitechno/wallpaper/$(file)) \
