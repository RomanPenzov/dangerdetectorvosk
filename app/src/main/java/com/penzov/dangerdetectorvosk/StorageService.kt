package com.penzov.dangerdetectorvosk

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

object StorageService {
    fun unpackAssets(context: Context, assetPath: String): File {
        val assetManager = context.assets
        val outDir = File(context.filesDir, assetPath)
        if (outDir.exists()) return outDir // ✅ Уже распаковано — не трогаем

        val files = assetManager.list(assetPath) ?: return outDir

        outDir.mkdirs()

        for (filename in files) {
            val inPath = "$assetPath/$filename"
            val outFile = File(outDir, filename)
            val subFiles = assetManager.list(inPath)
            if (!subFiles.isNullOrEmpty()) {
                // Рекурсивно копируем папку
                unpackAssets(context, inPath)
            } else {
                assetManager.open(inPath).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
        return outDir
    }
}
