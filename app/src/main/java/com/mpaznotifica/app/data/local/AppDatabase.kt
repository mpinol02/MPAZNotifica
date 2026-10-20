
package com.mpaznotifica.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction

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
        EnvioEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Permite acceder a las operaciones de comunicaciones.
    abstract fun comunicacionDao(): ComunicacionDao

    // Permite acceder a los resultados de envíos simulados.
    abstract fun envioDao(): EnvioDao

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

        // Crear o recuperar la instancia de Room.
        fun obtenerInstancia(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpaz_notifica_db"
                )
                    // Solo para desarrollo con datos ficticios.
                    // Puede borrar datos al reconstruir el esquema.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instancia

                instancia
            }
        }
    }
}
