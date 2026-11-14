
package com.mpaznotifica.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Base de datos local de MPAZ Notifica.
 *
 * Permite almacenar los comunicados ficticios
 * de la aplicación utilizando Room y SQLite.
 *
 * RF-09: historial de comunicados.
 * RF-10: consulta y filtros del historial.
 * RF-11: almacenamiento de información.
 *
 * Se utiliza una única instancia de la base de datos.
 */
@Database(
    entities = [
        ComunicacionEntity::class,
        EnvioEntity::class,
        ApoderadoEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Permite acceder a las operaciones de comunicaciones.
    abstract fun comunicacionDao(): ComunicacionDao

    // Permite acceder a los resultados de envíos simulados.
    abstract fun envioDao(): EnvioDao

    // Permite acceder a los apoderados ficticios.
    abstract fun apoderadoDao(): ApoderadoDao

    /**
     * Guarda un comunicado junto con todos sus envíos.
     *
     * Si ocurre un error, Room revierte la transacción
     * completa y evita guardar información incompleta.
     */
    suspend fun guardarComunicadoConEnvios(
        comunicacion: ComunicacionEntity,
        envios: List<EnvioEntity>
    ) {
        withTransaction {
            comunicacionDao().insertar(comunicacion)
            envioDao().insertarTodos(envios)
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migración de versión 2 a versión 3.
         *
         * Incorpora la tabla de apoderados sin eliminar
         * los comunicados ni envíos existentes.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `apoderados` (
                        `id` TEXT NOT NULL,
                        `nombreCompleto` TEXT NOT NULL,
                        `correo` TEXT NOT NULL,
                        `telefono` TEXT NOT NULL,
                        `canalPreferente` TEXT NOT NULL,
                        `pupilosIds` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Migración de versión 3 a versión 4.
         *
         * Agrega la ruta de la copia interna del PDF adjunto.
         * Los comunicados anteriores quedan con la ruta en NULL.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    "ALTER TABLE `comunicaciones` " +
                            "ADD COLUMN `adjuntoPdfRuta` TEXT"
                )
            }
        }

        // Crear o recuperar la instancia de Room.
        fun obtenerInstancia(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpaz_notifica_db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .build()

                INSTANCE = instancia
                instancia
            }
        }
    }
}
