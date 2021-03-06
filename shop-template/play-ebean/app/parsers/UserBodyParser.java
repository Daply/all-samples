package parsers;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import play.libs.F;
import play.libs.streams.Accumulator;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.Executor;

public class UserBodyParser implements BodyParser<User> {

    private BodyParser.Json jsonParser;
    private Executor executor;

    @Inject
    public UserBodyParser(Json jsonParser, Executor executor) {
        this.jsonParser = jsonParser;
        this.executor = executor;
    }

    @Override
    public Accumulator<ByteString, F.Either<Result, User>> apply(Http.RequestHeader request) {
        Accumulator<ByteString, F.Either<Result, JsonNode>> accumulator = jsonParser.apply(request);
        return accumulator.map(resultJsonNodeEither -> {
            if (resultJsonNodeEither.left.isPresent()) {
                return F.Either.Left(resultJsonNodeEither.left.get());
            }
            else {
                JsonNode json = resultJsonNodeEither.right.get();
                try {
                    User user = play.libs.Json.fromJson(json, User.class);
                    return F.Either.Right(user);
                }
                catch (Exception e) {
                    return F.Either.Left(Results.badRequest("Cant read user from json " + e.getMessage()));
                }
            }
        }, executor);
    }
}
