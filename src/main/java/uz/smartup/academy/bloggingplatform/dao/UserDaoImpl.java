package uz.smartup.academy.bloggingplatform.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uz.smartup.academy.bloggingplatform.entity.Post;
import uz.smartup.academy.bloggingplatform.entity.Comment;
import uz.smartup.academy.bloggingplatform.entity.Role;
import uz.smartup.academy.bloggingplatform.entity.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class UserDaoImpl implements UserDao {
    private final EntityManager entityManager;

    public UserDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<User> getALlUsers() {
        TypedQuery<User> users = entityManager.createQuery("FROM User", User.class);
        return users.getResultList();
    }

    @Override
    public User save(User user) {
        entityManager.persist(user);
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        try {
            TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username);

            return query != null ? query.getSingleResult() : null;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email);

            return query != null ? query.getSingleResult() : null;
        } catch (NoResultException e) {
            return null;
        }
    }


    @Override
    public User getUserById(int id) {
        return entityManager.find(User.class, id);
    }

    @Override
    @Transactional
    public void update(User user) {
        entityManager.merge(user);
    }

    @Override
    @Transactional
    public void delete(User user) {
        if (entityManager.contains(user)) {
            entityManager.remove(user);
        } else {
            User managedUser = entityManager.find(User.class, user.getId());
            if (managedUser != null) {
                entityManager.remove(managedUser);
            }
        }
    }

    @Override
    public List<Post> getUserAllPosts(int userId) {
        return entityManager.createQuery("SELECT p FROM Post p WHERE p.author.id = :userId", Post.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<Role> userFindByRoles(String userName) {
        TypedQuery<Role> query = entityManager.createQuery(
                "SELECT r FROM Role r WHERE r.id.username = :userName", Role.class);
        query.setParameter("userName", userName);
        return query.getResultList();
    }

    @Override
    public void updateUserComment(int userId, int postId, Comment comment) {
        User user = getUserById(userId);
        Post post = entityManager.find(Post.class, postId);
        comment.setPost(post);
        comment.setAuthor(user);
        entityManager.merge(comment);
    }

    @Override
    public User findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery("FROM User WHERE email = :email", User.class);

        query.setParameter("email", email);

        return query.getSingleResult();
    }

    @Override
    public Set<Role> getUserRoles(int userId) {
        User user = getUserById(userId);

        TypedQuery<Role> query = entityManager.createQuery("FROM Role r where r.id.username = :username", Role.class);
        query.setParameter("username", user.getUsername());

        return new HashSet<>(query.getResultList());

    }

    @Override
    public void saveRole(Role role) {
        entityManager.persist(role);
    }

    @Transactional
    @Override
    public List<User> findAllByEnabledIsNull() {
        TypedQuery<User> query = entityManager.createQuery("from User where enabled is null", User.class);
        return query.getResultList();
    }


    @Transactional
    public List<User> getUsersWithEditorRole() {
        String hql = "SELECT u FROM User u JOIN Role r ON u.username = r.id.username WHERE r.id.role = 'ROLE_EDITOR' AND u.enabled IS NOT NULL";
        TypedQuery<User> query = entityManager.createQuery(hql, User.class);
        return query.getResultList();
    }

    @Transactional
    public List<User> getUsersWithoutEditorRole() {
        String hql = "SELECT u FROM User u WHERE NOT EXISTS (SELECT r FROM Role r WHERE r.id.username = u.username AND r.id.role = 'ROLE_EDITOR')";
        TypedQuery<User> query = entityManager.createQuery(hql, User.class);
        return query.getResultList();


    }


}

/*

    tags creation
    pagination
    markdown

 */
