package com.penzov.dangerdetectorvosk

import android.content.Context
import java.io.File
import java.io.FileOutputStream

// 📦 Утилита для копирования моделей из assets в файловую систему устройства
object StorageService {
    fun unpackAssets(context: Context, assetPath: String): File {
        val assetManager = context.assets
        val outDir = File(context.filesDir, assetPath)

        if (outDir.exists()) return outDir

        val files = assetManager.list(assetPath) ?: return outDir
        outDir.mkdirs()

        for (filename in files) {
            val inPath = "$assetPath/$filename"
            val outFile = File(outDir, filename)
            val subFiles = assetManager.list(inPath)

            if (!subFiles.isNullOrEmpty()) {
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
