/*
 * port_save.c — File-backed EEPROM emulation for the PC port.
 *
 * The GBA Minish Cap uses 8 KB EEPROM (1024 blocks of 8 bytes).
 * This module stores the EEPROM data in "tmc.sav" next to the executable.
 *
 * Implements the four EEPROM BIOS functions:
 *   EEPROMConfigure(u16 type)
 *   EEPROMRead(u16 block, u16* dest)
 *   EEPROMWrite0_8k_Check(u16 block, const u16* src)
 *   EEPROMCompare(u16 block, const u16* src)
 */

#include "port_types.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define EEPROM_SIZE 8192                           /* 8 KB */
#define EEPROM_BLOCK 8                             /* 8 bytes per block */
#define EEPROM_BLOCKS (EEPROM_SIZE / EEPROM_BLOCK) /* 1024 */
#define SAVE_FILENAME "tmc.sav"

static const char* GetSavePath(void) {
    static char sPath[4096];
    const char* envDir = getenv("TMC_ANDROID_RUNTIME_DIR");
    if (envDir && envDir[0] != '\0') {
        snprintf(sPath, sizeof(sPath), "%s/%s", envDir, SAVE_FILENAME);
    } else {
        snprintf(sPath, sizeof(sPath), SAVE_FILENAME);
    }
    return sPath;
}

static u8 sEeprom[EEPROM_SIZE];
static int sEepromDirty = 0; /* set on write, cleared on flush */
static int sEepromInited = 0;

/* ---- Persistence -------------------------------------------------------- */

static void LoadEepromFile(void) {
    const char* path = GetSavePath();
    FILE* f = fopen(path, "rb");
    if (f) {
        fread(sEeprom, 1, EEPROM_SIZE, f);
        fclose(f);
        fprintf(stderr, "[SAVE] Loaded save file: %s\n", path);
    } else {
        memset(sEeprom, 0xFF, EEPROM_SIZE); /* blank EEPROM = 0xFF */
        fprintf(stderr, "[SAVE] No save file found at %s, starting fresh.\n", path);
    }
}

static void FlushEepromFile(void) {
    if (!sEepromDirty)
        return;
    const char* path = GetSavePath();
    FILE* f = fopen(path, "wb");
    if (f) {
        fwrite(sEeprom, 1, EEPROM_SIZE, f);
        fclose(f);
        sEepromDirty = 0;
        fprintf(stderr, "[SAVE] Flushed save file: %s\n", path);
    } else {
        fprintf(stderr, "[SAVE] ERROR: Could not write %s\n", path);
    }
}

/* ---- EEPROM BIOS API ---------------------------------------------------- */

u16 EEPROMConfigure(u16 type) {
    if (!sEepromInited) {
        LoadEepromFile();
        sEepromInited = 1;
    }
    /* type = 0x40 → 8 KB, type = 4 → 512 B. We always emulate 8 KB. */
    return 0; /* success */
}

u16 EEPROMRead(u16 block, u16* dest) {
    if (!sEepromInited) {
        LoadEepromFile();
        sEepromInited = 1;
    }
    if (block >= EEPROM_BLOCKS)
        return 0x80FF; /* EEPROM_OUT_OF_RANGE */

    memcpy(dest, &sEeprom[block * EEPROM_BLOCK], EEPROM_BLOCK);
    return 0; /* success */
}

u16 EEPROMWrite0_8k_Check(u16 block, const u16* src) {
    if (!sEepromInited) {
        LoadEepromFile();
        sEepromInited = 1;
    }
    if (block >= EEPROM_BLOCKS)
        return 0x80FF; /* EEPROM_OUT_OF_RANGE */

    memcpy(&sEeprom[block * EEPROM_BLOCK], src, EEPROM_BLOCK);
    sEepromDirty = 1;

    /* Flush to disk immediately to avoid data loss on crash */
    FlushEepromFile();
    return 0; /* success */
}

u16 EEPROMCompare(u16 block, const u16* src) {
    if (!sEepromInited) {
        LoadEepromFile();
        sEepromInited = 1;
    }
    if (block >= EEPROM_BLOCKS)
        return 0x80FF; /* EEPROM_OUT_OF_RANGE */

    if (memcmp(&sEeprom[block * EEPROM_BLOCK], src, EEPROM_BLOCK) != 0)
        return 0x8000; /* EEPROM_COMPARE_FAILED */

    return 0; /* match */
}
