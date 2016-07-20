package su.asgor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import su.asgor.dao.UserRepository;
import su.asgor.dao.VerificationTokenRepository;
import su.asgor.model.User;
import su.asgor.model.VerificationToken;
import su.asgor.service.MailService;
import su.asgor.service.PropertyService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@RestController
public class AuthAndRegController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private VerificationTokenRepository tokenRepository;
	@Autowired
	private MailService mailService;
	@Autowired
	private PropertyService propertyService;

    @RequestMapping("/user")
    public Principal user(Principal user) {
        return user;
    }

	@RequestMapping(value = "/user-admin",method = RequestMethod.POST)
	public ResponseEntity<?> admin(@RequestBody Map<String,String> map) {
		if(map.get("username").equals("admin")&&map.get("password").equals(propertyService.get("app.admin.password"))){
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("admin",
					propertyService.get("app.admin.password"),
					Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
			SecurityContextHolder.getContext().setAuthentication(token);
			return new ResponseEntity<Object>(HttpStatus.OK);
		}else {
			return new ResponseEntity<Object>(HttpStatus.UNAUTHORIZED);
		}
	}

    @RequestMapping(value = "/registrate",method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> registrate( @RequestBody User user, HttpServletRequest request) {
    	String baseUrl = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());
    	if(userRepository.findByEmail(user.getEmail()) == null) {
            VerificationToken token = new VerificationToken(UUID.randomUUID().toString(), user);
    		mailService.sendConfirmRegistration(user.getEmail(), baseUrl+"#/verify/" + token.getToken());
            userRepository.save(user);
            tokenRepository.save(token);
    		return new ResponseEntity<>(HttpStatus.OK);
    	} else {
    		return new ResponseEntity<>(HttpStatus.CONFLICT);
    	}
    }

    @RequestMapping(value = "/verify/{token}",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> verify(@PathVariable String token) {
    	VerificationToken vt = tokenRepository.findByToken(token);
        if((vt == null))
            return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	if(vt.getExpiryDate().after(new Date())){
    		User user = vt.getUser();
    		user.setEnabled(true);
    		userRepository.save(user);
    		tokenRepository.delete(vt);
    		return new ResponseEntity<>(HttpStatus.OK);
    	}else{
    		return new ResponseEntity<>(HttpStatus.CONFLICT);
    	}
    }

    @RequestMapping(value = "/resend/{token}",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> resend(@PathVariable String token, HttpServletRequest request) {
    	String baseUrl = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());
    	VerificationToken vt = tokenRepository.findByToken(token);
    	if((vt != null)){
    		VerificationToken newToken = new VerificationToken(UUID.randomUUID().toString(), vt.getUser());
    		tokenRepository.delete(vt);
    		tokenRepository.save(newToken);
    		mailService.sendConfirmRegistration(newToken.getUser().getEmail(), baseUrl+"#/verify/" + newToken.getToken());
    		return new ResponseEntity<>(HttpStatus.OK);
    	}else{
    		return new ResponseEntity<>(HttpStatus.CONFLICT);
    	}
    }

    @RequestMapping(value = "/recovery",method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> recover( @RequestBody String email, HttpServletRequest request) {
    	String baseUrl = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());
    	User user  = userRepository.findByEmail(email);
    	if(user != null) {
    		VerificationToken token = new VerificationToken(UUID.randomUUID().toString(), user);
    		tokenRepository.save(token);
    		mailService.sendPasswordRecovery(email, baseUrl+"#/recovery/" + token.getToken());
    		return new ResponseEntity<>(HttpStatus.OK);
    	} else {
    		return new ResponseEntity<>(HttpStatus.CONFLICT);
    	}
    }

    @RequestMapping(value = "/verify/recovery/{token}",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> verifyRecoveryToken(@PathVariable String token) {
        VerificationToken vt = tokenRepository.findByToken(token);
        if((vt == null))
            return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if(vt.getExpiryDate().after(new Date())){
            return new ResponseEntity<>(HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value = "/recovery/{token}",method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> recoverPassword(@PathVariable String token, @RequestBody String password) {
    	VerificationToken vt = tokenRepository.findByToken(token);
        if((vt == null))
            return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if(vt.getExpiryDate().after(new Date())){
    		User user = vt.getUser();
    		user.setPassword(password);
    		userRepository.save(user);
    		tokenRepository.delete(vt);
    		return new ResponseEntity<>(HttpStatus.OK);
    	}else{
    		return new ResponseEntity<>(HttpStatus.CONFLICT);
    	}
    }

    @RequestMapping(value = "/resend-password/{token}",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> resendPassword(@PathVariable String token, HttpServletRequest request) {
    	String baseUrl = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());
    	VerificationToken vt = tokenRepository.findByToken(token);
    	if((vt != null)){
    		VerificationToken newToken = new VerificationToken(UUID.randomUUID().toString(), vt.getUser());
    		tokenRepository.delete(vt);
    		tokenRepository.save(newToken);
    		mailService.sendPasswordRecovery(newToken.getUser().getEmail(), baseUrl+"#/recovery/" + newToken.getToken());
    		return new ResponseEntity<>(HttpStatus.OK);
    	}else{
    		return new ResponseEntity<>(HttpStatus.CONFLICT);
    	}
    }
}