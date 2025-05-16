
package uts.demo.controller;

import uts.demo.model.ToDo;
import uts.demo.model.User;
import uts.demo.repository.ToDoRepository;
import uts.demo.repository.UserRepository;
import uts.demo.service.ToDoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.Optional;

@Controller
@RequestMapping("/todos")
public class ToDoController {

    @Autowired
    private ToDoService toDoService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showTodos(@RequestParam(name = "filter", required = false) String filter,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {

        Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());
        if (optionalUser.isEmpty()) {
            return "redirect:/login?error";
        }

        User user = optionalUser.get();
        List<ToDo> todos = toDoService.getFilteredTodosByUser(user, filter != null ? filter : "");
        model.addAttribute("todos", todos);
        model.addAttribute("username", user.getUsername());
        return "todos";
    }

    @PostMapping("/create")
    public String createTodo(@RequestParam("task") String task,
                             @AuthenticationPrincipal UserDetails userDetails) {

        Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());
        if (optionalUser.isPresent()) {
            ToDo newTodo = new ToDo(task);
            newTodo.setUser(optionalUser.get());
            toDoService.saveTodo(newTodo);
        }
        return "redirect:/todos";
    }

    @PostMapping("/delete/{id}")
    public String deleteTodo(@PathVariable Long id) {
        toDoService.deleteTodo(id);
        return "redirect:/todos";
    }

    @PostMapping("/toggle/{id}")
    public String toggleTodo(@PathVariable Long id) {
        Optional<ToDo> optional = toDoService.getTodoById(id);
        if (optional.isPresent()) {
            ToDo todo = optional.get();
            todo.setCompleted(!todo.isCompleted());
            toDoService.saveTodo(todo);
        }
        return "redirect:/todos";
    }

   
    @PostMapping("/edit")
    public String editTodo(@RequestParam("id") Long id,
                           @RequestParam("task") String task,
                           @AuthenticationPrincipal UserDetails userDetails) {

        Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());
        if (optionalUser.isEmpty()) return "redirect:/login";

        Optional<ToDo> optional = toDoService.getTodoById(id);
        if (optional.isPresent()) {
            ToDo todo = optional.get();
            // Pastikan hanya user yang punya tugas bisa edit
            if (todo.getUser().getUsername().equals(userDetails.getUsername())) {
                todo.setTask(task);
                toDoService.saveTodo(todo);
            }
        }
        return "redirect:/todos";
    }
}