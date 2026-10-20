
package com.mpaznotifica.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mpaznotifica.app.R
import com.mpaznotifica.app.viewmodel.MpazNotificaViewModel

/**
 * Módulo "Autenticación y perfiles": login diferenciado para Inspectoría General y
 * Profesor/a Jefe. El apoderado no inicia sesión (no opera la app).
 *
 * La pantalla está completa, pero viewModel.login() es TODO(equipo): por ahora siempre
 * rechaza. Mientras tanto, los botones "Modo desarrollo" permiten entrar con cada perfil.
 *
 * Usuarios de prueba (MockData.usuarios):
 *   inspectoria@mpaznotifica.cl / inspectoria123
 *   profesor.4basicoA@mpaznotifica.cl / profesor123
 */
@Composable
fun LoginScreen(
    viewModel: MpazNotificaViewModel,
    onIngresar: () -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }

    val error by viewModel.errorLogin.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo_app),
                contentDescription = "Logo",
                modifier = Modifier.size(110.dp)
            )

            Text(
                "MPAZ Notifica",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                "Ingreso de personal docente y directivo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it },
                label = { Text("Correo institucional") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Clave de acceso") },
                singleLine = true,
                visualTransformation = if (claveVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { claveVisible = !claveVisible }
                    ) {
                        Icon(
                            imageVector = if (claveVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (claveVisible) {
                                "Ocultar clave"
                            } else {
                                "Mostrar clave"
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Text(
                    error.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (viewModel.login(usuario.trim(), clave)) {
                        onIngresar()
                    }
                }
            ) {
                Text("Ingresar")
            }

            // ---- SOLO PARA DESARROLLO ---- TODO(equipo): eliminar antes de entregar.
            // Resuelto por el equipo: se eliminaron los botones de acceso
            // sin credenciales. Ahora se utiliza únicamente el login validado.
        }
    }
}
