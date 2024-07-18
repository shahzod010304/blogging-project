package uz.smartup.academy.bloggingplatform.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.smartup.academy.bloggingplatform.entity.*;

import java.util.List;

public interface PostDao {
    void save(Post post);

    void update(Post post);

    void delete(Post post);

    Post getById(int id);

    List<Post> getAllPosts();

    List<Post> getPostsByTag(Tag tag);

    List<Post> getPostsByCategory(Category category);

    User getAuthorById(int id);

    List<Post> getPostsByAuthor(int authorId);

    List<Comment> getPostComments(int id);

    List<Post> findPostsByStatus(Post.Status status);

    List<Post> findPostByStatusAndAuthorId(Post.Status status, int authorId);

    Post.Status findPostStatusById(int postId);

    List<Post> searchPosts(String keyword);
}

/*
post:
    save
    update
    delete
    getById
    getPostsByTag
    getByPostsByCategory
    getAuthorById
    getPostsByAuthor
 */
