package edu.demo.notificationservice.notification.persistence;

import edu.demo.notificationservice.notification.application.NotificationRepository;
import edu.demo.notificationservice.notification.domain.Notification;
import edu.demo.notificationservice.notification.domain.NotificationFilter;
import edu.demo.notificationservice.notification.domain.ReadFilter;
import edu.demo.notificationservice.notification.domain.RecipientFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * MongoDB adapter for {@link NotificationRepository}. Translates {@link NotificationFilter} into a query with the
 * same semantics as {@link NotificationFilter#matches(Notification)}.
 */
@Repository
class MongoNotificationRepository implements NotificationRepository {

    private final MongoOperations mongo;

    MongoNotificationRepository(MongoOperations mongo) {
        this.mongo = mongo;
    }

    @Override
    public boolean insertIfAbsent(Notification notification) {
        try {
            mongo.insert(NotificationDocument.from(notification));
            return true;
        } catch (DuplicateKeyException alreadyStored) {
            // The unique _id is the idempotency guard: a duplicate means this notification was generated before.
            return false;
        }
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return Optional.ofNullable(mongo.findById(id.toString(), NotificationDocument.class))
                .map(NotificationDocument::toDomain);
    }

    @Override
    public List<Notification> findNewestFirst(NotificationFilter filter) {
        var query = queryFor(filter).with(Sort.by(Sort.Direction.DESC, NotificationDocument.CREATED_AT));
        return mongo.find(query, NotificationDocument.class).stream()
                .map(NotificationDocument::toDomain)
                .toList();
    }

    @Override
    public void replace(Notification notification) {
        mongo.save(NotificationDocument.from(notification));
    }

    private static Query queryFor(NotificationFilter filter) {
        var query = new Query();
        recipientCriteria(filter.recipient()).ifPresent(query::addCriteria);
        readCriteria(filter.read()).ifPresent(query::addCriteria);
        return query;
    }

    private static Optional<Criteria> recipientCriteria(RecipientFilter filter) {
        return switch (filter) {
            case RecipientFilter.AnyRecipient _ -> Optional.empty();
            case RecipientFilter.VisibleTo visibleTo -> Optional.of(new Criteria().orOperator(
                    where(NotificationDocument.RECIPIENT).is(visibleTo.name()),
                    where(NotificationDocument.RECIPIENT).isNull()));
        };
    }

    private static Optional<Criteria> readCriteria(ReadFilter filter) {
        return switch (filter) {
            case ANY -> Optional.empty();
            case UNREAD -> Optional.of(where(NotificationDocument.READ_AT).isNull());
            case READ -> Optional.of(where(NotificationDocument.READ_AT).ne(null));
        };
    }
}
