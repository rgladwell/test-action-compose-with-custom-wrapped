import scala.concurrent.Future
import play.api.mvc.{Request, WrappedRequest}
import play.api.mvc.ActionBuilder
import play.api.mvc.ActionFilter
import play.api.mvc.Result
import play.api.mvc.Results.Unauthorized
import play.api.mvc.Controller

case class AuthRequest[A, U](val request: Request[A], val user: U) extends WrappedRequest[A](request)

case class TestUser(id: String)

case class AuthenticationAction[A]() extends ActionBuilder[({type λ[B] = AuthRequest[B, TestUser]})#λ] {

  override def invokeBlock[A](request: Request[A], block: (AuthRequest[A, TestUser]) => Future[Result]): Future[Result] =
    block(AuthRequest(request, TestUser("test-id")))

}

case class AuthorisedAction[A]() extends ActionFilter[({type λ[B] = AuthRequest[B, TestUser]})#λ] {

  def authorised(user: TestUser) = true

  override def filter[A](request: AuthRequest[A, TestUser]) = Future.successful {
    if( authorised(request.user) ) None
    else Some(Unauthorized)
  }

}

class ExampleController extends Controller {
  
  def secured = (AuthenticationAction() andThen AuthorisedAction()) { request =>
    Ok(request.user.id)
  }

}
