package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {
  
  public static Result login() {
    return ok(login.render("Your new application is ready."));
  }

  
}