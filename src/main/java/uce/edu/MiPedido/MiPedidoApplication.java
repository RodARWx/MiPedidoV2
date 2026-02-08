package uce.edu.MiPedido;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import uce.edu.MiPedido.Model.Rol;
import uce.edu.MiPedido.Model.Usuario;
import uce.edu.MiPedido.Repository.UsuarioRepository;
import uce.edu.MiPedido.Service.UsuarioSesionService;

@SpringBootApplication
public class MiPedidoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiPedidoApplication.class, args);

    }

    @Autowired
    private UsuarioSesionService usuarioSesionService;

    public void run(String... args) throws Exception {

        // CAMBIA AQUÍ según lo que quieras probar
        usuarioSesionService.loginComoAdmin();
        // usuarioSesionService.loginComoMesero();

        System.out.println("Usuario simulado: "
                + usuarioSesionService.getUsuarioActual().getRol());
    }

    @Bean
    CommandLineRunner initUsuarios(UsuarioRepository usuarioRepo) {
        return args -> {

            // ADMIN por defecto
            if (usuarioRepo.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword("admin123"); // luego lo cambiamos
                admin.setRol(Rol.ADMIN);
                usuarioRepo.save(admin);
            }

            // MESERO de prueba
            if (usuarioRepo.findByUsername("mesero1").isEmpty()) {
                Usuario mesero = new Usuario();
                mesero.setUsername("mesero1");
                mesero.setPassword("1234"); // temporal
                mesero.setRol(Rol.MESERO);
                usuarioRepo.save(mesero);
            }
        };

    }
}
