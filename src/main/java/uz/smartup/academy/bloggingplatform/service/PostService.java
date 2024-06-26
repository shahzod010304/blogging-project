package uz.smartup.academy.bloggingplatform.service;

import uz.smartup.academy.bloggingplatform.dto.PostDto;
import uz.smartup.academy.bloggingplatform.entity.Post;
import uz.smartup.academy.bloggingplatform.entity.User;

import java.util.List;

public interface PostService {
    void createPost(Post post);

    void update(Post post);

    void delete(int postId);

    PostDto getById(int id);

    List<PostDto> getPostsByTag(int tagId);

    List<PostDto> getPostsByCategory(int categoryId);

    User getAuthorById(int id);

    List<PostDto> getPostsByAuthor(int authorId);
}
