package IntegracionBackFront.backfront.Controller.Categories;

import IntegracionBackFront.backfront.Exceptions.Category.ExceptionCategoryNotFound;
import IntegracionBackFront.backfront.Exceptions.Category.ExceptionColumnDuplicate;
import IntegracionBackFront.backfront.Models.DTO.Categories.CategoryDTO;
import IntegracionBackFront.backfront.Models.DTO.Products.ProductDTO;
import IntegracionBackFront.backfront.Services.Categories.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService service;

    @GetMapping("/getDataCategory")
    //Cambiamos a RequestEntity para que use los request, ademas se pone page y dto
    private ResponseEntity<Page<CategoryDTO>> getData(
            //Determinamos los valores por defecto de la paginacion
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size){
        //Si los registro son mayores a 0 o son menores a 50
        if (size <= 0 || size > 50){
            //Mandamos un error para que el usuario no pueda pedir mas de 50 valores a la vez para que asi no trone la API
            ResponseEntity.badRequest().body(Map.of(
                    "status", "El tamaño de la página debe estar entre 1 y 50"
            ));
            //Manda un null al usuario ya que no va a buscar nada
            return ResponseEntity.ok(null);
        }
        //Crea el objeto categories y manda a llamar a la funcion del service, mandando los datos que se usaran en el pageable
        Page<CategoryDTO> categories = service.getAllCategories(page, size);
        //Valida si el categories viene null
        if (categories == null){
            //Manda un error que avisa que no hay registros
            ResponseEntity.badRequest().body(Map.of(
                    "status", "Error al obtener los datos"
            ));
        }
        //Regresa el page con los datos encontrados y manda un mensaje de ok
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/newCategory")
    private ResponseEntity<Map<String, Object>> inserCategory(@Valid @RequestBody CategoryDTO json, HttpServletRequest request){
        try{
            CategoryDTO response =service.insert(json);
            if (response == null){
                return ResponseEntity.badRequest().body(Map.of(
                        "Error", "Inserción incorrecta",
                        "Estatus", "Inserción incorrecta",
                        "Descripción", "Verifique los valores"
                ));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "Estado", "Completado",
                "data", response
            ));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Error al registrar Categoria",
                            "detail", e.getMessage()
                    ));
        }
    }

    @PutMapping("/updateCategory/{id}")
    public ResponseEntity<?> modificarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO usuario,
            BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            Map<String, String> errores = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errores.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errores);
        }

        try{
            CategoryDTO usuarioActualizado = service.update(id, usuario);
            return ResponseEntity.ok(usuarioActualizado);
        }
        catch (ExceptionCategoryNotFound e){
            return ResponseEntity.notFound().build();
        }
        catch (ExceptionColumnDuplicate e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("error", "Datos duplicados","campo", e.getColumnDuplicate())
            );
        }
    }

    // Mapea este metodo a una petición DELETE con un parámetro de ruta {id}
    @DeleteMapping("/deleteCategory/{id}")
    public ResponseEntity<Map<String, Object>> eliminarUsuario(@PathVariable Long id) {
        try {
            // Intenta eliminar la categoria usando objeto 'service'
            // Si el metodo delete retorna false (no encontró la categoria)
            if (!service.delete(id)) {
                // Retorna una respuesta 404 (Not Found) con información detallada
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        // Agrega un header personalizado
                        .header("X-Mensaje-Error", "Categoría no encontrada")
                        // Cuerpo de la respuesta con detalles del error
                        .body(Map.of(
                                "error", "Not found",  // Tipo de error
                                "mensaje", "La categoria no ha sido encontrada",  // Mensaje descriptivo
                                "timestamp", Instant.now().toString()  // Marca de tiempo del error
                        ));
            }

            // Si la eliminación fue exitosa, retorna 200 (OK) con mensaje de confirmación
            return ResponseEntity.ok().body(Map.of(
                    "status", "Proceso completado",  // Estado de la operación
                    "message", "Categoría eliminada exitosamente"  // Mensaje de éxito
            ));

        } catch (Exception e) {
            // Si ocurre cualquier error inesperado, retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "Error",  // Indicador de error
                    "message", "Error al eliminar la categoría",  // Mensaje general
                    "detail", e.getMessage()  // Detalles técnicos del error (para debugging)
            ));
        }
    }

}
