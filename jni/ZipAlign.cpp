/*
 * Zip alignment tool
 */
#include "ZipFile.h"

#include <stdlib.h>
#include <stdio.h>
#include <io.h>

using namespace android;

/*
 * Copy all entries from "pZin" to "pZout", aligning as needed.
 */
static int copyAndAlign(ZipFile *pZin, ZipFile *pZout, int alignment) {
    int numEntries = pZin->getNumEntries();
    ZipEntry *pEntry;
    int bias = 0;
    status_t status;

    for (int i = 0; i < numEntries; i++) {
        ZipEntry *pNewEntry;
        int padding = 0;

        pEntry = pZin->getEntryByIndex(i);
        if (pEntry == nullptr) {
            fprintf(stderr, "ERROR: unable to retrieve entry %d\n", i);
            return 1;
        }

        if (pEntry->isCompressed()) {
            printf("--- %s: orig at %ld len=%ld (compressed)\n",
                pEntry->getFileName(), (long) pEntry->getFileOffset(),
                (long) pEntry->getUncompressedLen());

        } else {
            long newOffset = pEntry->getFileOffset() + bias;
            padding = (alignment - (newOffset % alignment)) % alignment;

        }

        status = pZout->add(pZin, pEntry, padding, &pNewEntry);
        if (status != NO_ERROR)
            return 1;
        bias += padding;
        printf(" added '%s' at %ld (pad=%d)\n",
            pNewEntry->getFileName(), (long) pNewEntry->getFileOffset(),
            padding);
    }

    return 0;
}

/*
 * Process a file.  We open the input and output files, failing if the
 * output file exists and "force" wasn't specified.
 */
static int process(const char *inFileName, const char *outFileName,
                   int alignment, bool force) {
    ZipFile zin, zout;

    printf("PROCESS: align=%d in='%s' out='%s' force=%d\n",
        alignment, inFileName, outFileName, force);

    /* this mode isn't supported -- do a trivial check */
    if (strcmp(inFileName, outFileName) == 0) {
        fprintf(stderr, "Input and output can't be same file\n");
        return 1;
    }

    /* don't overwrite existing unless given permission */
    if (!force && access(outFileName, 0) == 0) {
        fprintf(stderr, "Output file '%s' exists\n", outFileName);
        return 1;
    }

    if (zin.open(inFileName, ZipFile::kOpenReadOnly) != NO_ERROR) {
        fprintf(stderr, "Unable to open '%s' as zip archive\n", inFileName);
        return 1;
    }
    if (zout.open(outFileName,
                  ZipFile::kOpenReadWrite | ZipFile::kOpenCreate | ZipFile::kOpenTruncate)
            != NO_ERROR) {
        fprintf(stderr, "Unable to open '%s' as zip archive\n", outFileName);
        return 1;
    }

    int result = copyAndAlign(&zin, &zout, alignment);
    if (result != 0) {
        printf("zipalign: failed rewriting '%s' to '%s'\n",
               inFileName, outFileName);
    }
    return result;
}

/*
 * Verify the alignment of a zip archive.
 */
static int verify(const char *fileName, int alignment, bool verbose) {
    ZipFile zipFile;
    bool foundBad = false;

    if (verbose)
        printf("Verifying alignment of %s (%d)...\n", fileName, alignment);

    if (zipFile.open(fileName, ZipFile::kOpenReadOnly) != NO_ERROR) {
        fprintf(stderr, "Unable to open '%s' for verification\n", fileName);
        return 1;
    }

    int numEntries = zipFile.getNumEntries();
    ZipEntry *pEntry;

    for (int i = 0; i < numEntries; i++) {
        pEntry = zipFile.getEntryByIndex(i);
        if (pEntry->isCompressed()) {
            if (verbose) {
                printf("%8ld %s (OK - compressed)\n",
                       (long) pEntry->getFileOffset(), pEntry->getFileName());
            }
        } else {
            long offset = pEntry->getFileOffset();
            if ((offset % alignment) != 0) {
                if (verbose) {
                    printf("%8ld %s (BAD - %ld)\n",
                           (long) offset, pEntry->getFileName(),
                           offset % alignment);
                }
                foundBad = true;
            } else {
                if (verbose) {
                    printf("%8ld %s (OK)\n",
                           (long) offset, pEntry->getFileName());
                }
            }
        }
    }

    if (verbose)
        printf("Verification %s\n", foundBad ? "FAILED" : "succesful");

    return foundBad ? 1 : 0;
}

/* The wrapper functions below were added by Hunter
 * Even though there aren't many lines, copyright still applies.
 *
 * Copyright (c) 2014 Hunter
 */


/**
 *
 * Align a zip file. This function will fail if out_filename exists and force is 0
 * It is recommended that you verify the out_filename using zipalign_is_aligned() upon success
 *
 * @return 1 on success, otherwise returns 0 on failure
 *
 */
extern "C" __declspec(dllexport)
int zipalign(const char *in_filename, const char *out_filename, int alignment, int force) {
    if(!in_filename || !out_filename) {
        return 0;
    }
    return process(in_filename, out_filename, alignment, force != 0) ? 0 : 1;
}


/**
 *
 * Checks if a zip file is aligned or not
 *
 * @return 1 if aligned, otherwise returns 0 on failure or unaligned
 *
 */
extern "C" __declspec(dllexport)
int zipalign_is_aligned(const char *filename, int alignment) {
    if(!filename) {
        return 0;
    }
    return verify(filename, alignment, false) ? 0 : 1;
}