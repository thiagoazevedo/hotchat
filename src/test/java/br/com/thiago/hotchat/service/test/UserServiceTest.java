package br.com.thiago.hotchat.service.test;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import br.com.thiago.hotchat.builder.UserBuilder;
import br.com.thiago.hotchat.dto.UserDTO;
import br.com.thiago.hotchat.entity.User;
import br.com.thiago.hotchat.exception.HotChatException;
import br.com.thiago.hotchat.repository.UserRepository;
import br.com.thiago.hotchat.service.UserService;
import br.com.thiago.hotchat.util.Messages;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private BCryptPasswordEncoder bcrypt;

	@MockBean
	private UserRepository userRepository;

	@Test
	public void saveNewUserSucess() throws HotChatException {
		User userMock = new UserBuilder().build();
		mockRepositoryFindByEmail(null, "unitteste@email.com.br");
		mockRepositorySave(userMock);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		User user = userService.save(userMock);
		assertNotNull(user);

		verify(userRepository).saveAndFlush(userCaptor.capture());

		User userBeforeSave = userCaptor.getValue();
		assertEquals(userBeforeSave.getName(), "Usuario Teste Unitário");
		assertEquals(userBeforeSave.getEmail(), "unitteste@email.com.br");
		assertTrue(bcrypt.matches("123456", userBeforeSave.getPassword()));
		assertEquals(userBeforeSave.isOnline(), false);
	}

	@Test
	public void saveNewUserErrorEmailRegistered() {
		try {
			User userMock = new UserBuilder().build();
			mockRepositoryFindByEmail(userMock, "unitteste@email.com.br");
			userService.save(userMock);
			fail("Teste deve falhar");
		} catch (HotChatException e) {
			assertEquals(e.getMessage(), Messages.emailRegistered());
		}
	}

	@Test
	public void findAllUsersConvertDTOSuccess() {
		User userMock1 = new UserBuilder().build();
		User userMock2 = new UserBuilder().withEmail("unitteste1@email.com.br").build();
		User userMock3 = new UserBuilder().withEmail("unitteste2@email.com.br").build();
		List<User> users = Arrays.asList(userMock1, userMock2, userMock3);
		when(userRepository.findAllByOrderByOnlineDescNameAsc()).thenReturn(users);
		List<UserDTO> usersDTO = userService.findAllUsersConvertDTO();
		assertThat(usersDTO, hasSize(3));
	}

	@Test
	public void loadUserByUsernameSucess() {
		User userMock = new UserBuilder().build();
		mockRepositoryFindByEmail(userMock, "unitteste@email.com.br");
		UserDetails userDetails = userDetailsService.loadUserByUsername("unitteste@email.com.br");
		assertNotNull(userDetails);
	}

	@Test(expected = UsernameNotFoundException.class)
	public void loadUserByUsernameError() {
		mockRepositoryFindByEmail(null, "unitteste@email.com.br");
		userDetailsService.loadUserByUsername("unitteste@email.com.br");
	}

	public void mockRepositoryFindByEmail(User userMock, String email) {
		when(userRepository.findByEmail(email)).thenReturn(userMock);
	}

	public void mockRepositorySave(User userMock) {
		User userMockSave = new UserBuilder().withId(1L).build();
		when(userRepository.saveAndFlush(userMock)).thenReturn(userMockSave);
	}
}
