#ifndef __LIB_UTILS_COMPAT_H
#define __LIB_UTILS_COMPAT_H

#include <io.h>

/* Compatibility definitions for non-Linux (i.e., BSD-based) hosts. */
#ifndef HAVE_OFF64_T

#ifndef loff_t
typedef long loff_t;
#endif

typedef loff_t off64_t;

inline off64_t lseek64(int fd, off64_t offset, int whence) {
    return _lseek(fd, offset, whence);
}

#ifdef HAVE_PREAD
inline ssize_t pread64(int fd, void *buf, size_t nbytes, off64_t offset) {
    return pread(fd, buf, nbytes, offset);
}
#endif

#endif /* !HAVE_OFF64_T */

#if HAVE_PRINTF_ZD
#  define ZD "%zd"
#  define ZD_TYPE ssize_t
#else
#  define ZD "%ld"
#  define ZD_TYPE long
#endif

/*
 * TEMP_FAILURE_RETRY is defined by some, but not all, versions of
 * <unistd.h>. (Alas, it is not as standard as we'd hoped!) So, if it's
 * not already defined, then define it here.
 */
#ifndef TEMP_FAILURE_RETRY
/* Used to retry syscalls that can return EINTR. */
#define TEMP_FAILURE_RETRY(exp) ({         \
    typeof (exp) _rc;                      \
    do {                                   \
        _rc = (exp);                       \
    } while (_rc == -1 && errno == EINTR); \
    _rc; })
#endif

#endif /* __LIB_UTILS_COMPAT_H */
