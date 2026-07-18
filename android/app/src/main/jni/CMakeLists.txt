cmake_minimum_required(VERSION 3.22.1)
project(tmc)

set(TMC_ROOT ../../../../../)

# Preprocessor defines
add_definitions(-DPC_PORT -DNON_MATCHING -DUSE_HDMA -DUSA -DENGLISH -DREVISION=0)
add_definitions(-DTMC_ANDROID_PORT -DTMC_ANDROID_EXPERIMENTAL)

# Include directories
include_directories(
    ${TMC_ROOT}/include
    ${TMC_ROOT}/libs
    ${TMC_ROOT}/port
    ${TMC_ROOT}
    ${TMC_ROOT}/build/USA
    ${TMC_ROOT}/libs/ViruaPPU/include
    ${TMC_ROOT}/libs/VirtuaAPU/include
    ${TMC_ROOT}/libs/agbplay_core
    ${TMC_ROOT}/tools/src/assets_extractor
)

# SDL3
add_subdirectory(SDL)
include_directories(SDL/include)

# Source files
set(SOURCES
    ${TMC_ROOT}/port/port_main.c
    ${TMC_ROOT}/port/port_audio.c
    ${TMC_ROOT}/port/port_runtime_config.cpp
    ${TMC_ROOT}/port/port_debug_menu.cpp
    ${TMC_ROOT}/port/port_debug_actions.c
    ${TMC_ROOT}/port/port_quicksave.c
    ${TMC_ROOT}/port/port_inline_ptrs.c
    ${TMC_ROOT}/port/port_asset_bootstrap.cpp
    ${TMC_ROOT}/port/port_asset_index.c
    ${TMC_ROOT}/port/port_update_check.c
    ${TMC_ROOT}/port/port_asset_loader.cpp
    ${TMC_ROOT}/port/port_asset_pipeline.cpp
    ${TMC_ROOT}/port/port_asset_log.cpp
    ${TMC_ROOT}/port/port_asset_pak.cpp
    ${TMC_ROOT}/port/port_asset_pak_loader.cpp
    ${TMC_ROOT}/port/port_m4a_backend.cpp
    ${TMC_ROOT}/port/generated_sounds_embed.cpp
    ${TMC_ROOT}/port/port_ppu.cpp
    ${TMC_ROOT}/port/port_rom.c
    ${TMC_ROOT}/port/port_stubs.c
    ${TMC_ROOT}/port/stubs_autogen.c
    ${TMC_ROOT}/port/data_stubs_autogen.c
    ${TMC_ROOT}/port/data_const_stubs.c
    ${TMC_ROOT}/port/port_rom_tables.c
    ${TMC_ROOT}/port/port_bios.c
    ${TMC_ROOT}/port/port_linked_stubs.c
    ${TMC_ROOT}/port/port_figurines.c
    ${TMC_ROOT}/port/port_draw.c
    ${TMC_ROOT}/port/port_gba_mem.c
    ${TMC_ROOT}/port/port_hdma.c
    ${TMC_ROOT}/port/port_upscale.c
    ${TMC_ROOT}/port/port_save.c
    ${TMC_ROOT}/port/port_softslots.c
    ${TMC_ROOT}/port/port_touch_controls.cpp
    ${TMC_ROOT}/port/port_android_jni.cpp
    ${TMC_ROOT}/port/port_filter.c
    ${TMC_ROOT}/port/port_animation.c
    ${TMC_ROOT}/port/port_math.c
    ${TMC_ROOT}/port/port_text_render.c
    ${TMC_ROOT}/port/port_gameplay_stubs.c
    ${TMC_ROOT}/port/port_m4a_stubs.c
    ${TMC_ROOT}/port/port_room_funcs.c
    ${TMC_ROOT}/port/port_script_funcs.c
    ${TMC_ROOT}/tools/src/assets_extractor/assets_extractor_api.cpp

    ${TMC_ROOT}/libs/ViruaPPU/src/mode0.c
    ${TMC_ROOT}/libs/ViruaPPU/src/mode1.c
    ${TMC_ROOT}/libs/ViruaPPU/src/mode2.c
    ${TMC_ROOT}/libs/ViruaPPU/src/mode7.c
    ${TMC_ROOT}/libs/ViruaPPU/src/virtuappu.c

    ${TMC_ROOT}/libs/VirtuaAPU/src/mode1_dmg.c
    ${TMC_ROOT}/libs/VirtuaAPU/src/mode2_cgb.c
    ${TMC_ROOT}/libs/VirtuaAPU/src/mode3_gba.c
    ${TMC_ROOT}/libs/VirtuaAPU/src/virtuapu.c

    ${TMC_ROOT}/libs/agbplay_core/CGBPatterns.cpp
    ${TMC_ROOT}/libs/agbplay_core/Debug.cpp
    ${TMC_ROOT}/libs/agbplay_core/LoudnessCalculator.cpp
    ${TMC_ROOT}/libs/agbplay_core/MP2KChn.cpp
    ${TMC_ROOT}/libs/agbplay_core/MP2KChnPSG.cpp
    ${TMC_ROOT}/libs/agbplay_core/MP2KChnPCM.cpp
    ${TMC_ROOT}/libs/agbplay_core/MP2KContext.cpp
    ${TMC_ROOT}/libs/agbplay_core/MP2KPlayer.cpp
    ${TMC_ROOT}/libs/agbplay_core/MP2KTrack.cpp
    ${TMC_ROOT}/libs/agbplay_core/Resampler.cpp
    ${TMC_ROOT}/libs/agbplay_core/ReverbEffect.cpp
    ${TMC_ROOT}/libs/agbplay_core/Rom.cpp
    ${TMC_ROOT}/libs/agbplay_core/SequenceReader.cpp
    ${TMC_ROOT}/libs/agbplay_core/SoundMixer.cpp
    ${TMC_ROOT}/libs/agbplay_core/Types.cpp
    ${TMC_ROOT}/libs/agbplay_core/Xcept.cpp
)

# Add game core files
file(GLOB_RECURSE GAME_SOURCES
    ${TMC_ROOT}/src/*.c
)
# Exclude original GBA library files and EEPROM code that are replaced by port stubs/impls
list(FILTER GAME_SOURCES EXCLUDE REGEX "src/gba/.*")
list(FILTER GAME_SOURCES EXCLUDE REGEX "src/eeprom.c")
list(APPEND SOURCES ${GAME_SOURCES})

add_library(main SHARED ${SOURCES})

target_link_libraries(main
    SDL3-shared
    log
    android
    m
)
