// The following ifdef block is the standard way of creating macros which make exporting
// from a DLL simpler. All files within this DLL are compiled with the ZIPALIGNJNI_EXPORTS
// symbol defined on the command line. This symbol should not be defined on any project
// that uses this DLL. This way any other project whose source files include this file see
// ZIPALIGNJNI_API functions as being imported from a DLL, whereas this DLL sees symbols
// defined with this macro as being exported.
#ifdef ZIPALIGNJNI_EXPORTS
#define ZIPALIGNJNI_API __declspec(dllexport)
#else
#define ZIPALIGNJNI_API __declspec(dllimport)
#endif

// This class is exported from the dll
class ZIPALIGNJNI_API CZipAlignJNI {
public:
	CZipAlignJNI(void);
	// TODO: add your methods here.
};

extern ZIPALIGNJNI_API int nZipAlignJNI;

ZIPALIGNJNI_API int fnZipAlignJNI(void);
