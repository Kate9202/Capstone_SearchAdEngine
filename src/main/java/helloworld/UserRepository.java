package helloworld;

import org.springframework.data.repository.CrudRepository;
//import helloworld.User;
//import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete
@Transactional
public interface UserRepository extends CrudRepository<User, Long>{

}
