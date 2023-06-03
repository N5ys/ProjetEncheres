package fr.eni.projetenchere.servlet;

import java.io.IOException;
import java.sql.SQLException;

import fr.eni.projetenchere.bll.UserManager;
import fr.eni.projetenchere.bll.CodeErreur;
import fr.eni.projetenchere.bo.User;
import fr.eni.projetenchere.exception.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet(name = "modifyUserProfile", value = "/ModifyUserProfile")
public class ModifyUserProfile extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Vérifiez si l'utilisateur est connecté
		if (request.getSession().getAttribute("utilisateurConnecte") == null) {
			// Si non, redirection vers la page de connexion ou afficher un message d'erreur
			request.setAttribute("message", "Vous n'êtes pas connecté");
			request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
		} else {
			// Si oui, affichez la page de modification de profil avec les informations actuelles de l'utilisateur
			User user = (User) request.getSession().getAttribute("utilisateurConnecte");
			request.setAttribute("user", user);
			request.getRequestDispatcher("/WEB-INF/views/modifyUserProfile.jsp").forward(request, response);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String pseudo = request.getParameter("pseudo");
		String nom = request.getParameter("nom");
		String prenom = request.getParameter("prenom");
		String email = request.getParameter("email");
		String telephone = request.getParameter("telephone");
		String rue = request.getParameter("rue");
		String codePostal = request.getParameter("codePostal");
		String ville = request.getParameter("ville");
		String motDePasse = request.getParameter("motDePasse");
		String newPassword = request.getParameter("newMdp");
		String confirmationMdp = request.getParameter("confirmationMdp");

		UserManager userManager = UserManager.getInstance();
		BusinessException businessException = new BusinessException();

		User user = (User) request.getSession().getAttribute("utilisateurConnecte");
		try {
			// Check if the current password is correct before allowing updates
			if (!userManager.checkPassword(user.getNoUtilisateur(), motDePasse)) {
				businessException.ajouterErreur(CodeErreur.MDP_INCORRECT);
			} else {
				try {
					if (pseudo != null && !pseudo.equals(user.getPseudo())) {
						if (userManager.checkPseudoAvailability(pseudo)) {
							businessException.ajouterErreur(CodeErreur.PSEUDO_EXISTANT);
						} else {
							user.setPseudo(pseudo);
						}
					}

					if (nom != null && !nom.equals(user.getNom())) {
						user.setNom(nom);
					}

					if (prenom != null && !prenom.equals(user.getPrenom())) {
						user.setPrenom(prenom);
					}

					if (email != null && !email.equals(user.getEmail())) {
						if (userManager.checkEmailAvailability(email)) {
							businessException.ajouterErreur(CodeErreur.EMAIL_EXISTANT);
						} else {
							user.setEmail(email);
						}
					}

					if (telephone != null && !telephone.equals(user.getTelephone())) {
						user.setTelephone(telephone);
					}

					if (rue != null && !rue.equals(user.getRue())) {
						user.setRue(rue);
					}

					if (codePostal != null && !codePostal.equals(user.getCodePostal())) {
						user.setCodePostal(codePostal);
					}

					if (ville != null && !ville.equals(user.getVille())) {
						user.setVille(ville);
					}

					if (newPassword != null && confirmationMdp != null && newPassword.equals(confirmationMdp)) {
						if (!userManager.validateMotDePasse(newPassword)) {
							businessException.ajouterErreur(CodeErreur.MDP_INVALIDE);
						} else if (newPassword != null && confirmationMdp != null && newPassword.equals(confirmationMdp)) {
							if(!userManager.validateMotDePasse(newPassword)) {
								businessException.ajouterErreur(CodeErreur.MDP_INVALIDE);
							} else {
								user.setMotDePasse(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
							}
						}
					}

					if (businessException.hasErreurs()) {
						request.setAttribute("utilisateur", user);
						request.setAttribute("listeCodesErreur", businessException.getListeCodesErreur());
						request.getRequestDispatcher("/WEB-INF/views/modifyUserProfile.jsp").forward(request, response);
					} else {
						userManager.updateUser(user);

						User connected = userManager.selectUserByPseudoOuEmail(user.getPseudo());

						// Créer une nouvelle session avec l'utilisateur de la base de données
						request.getSession().setAttribute("utilisateurConnecte", connected);

						// Rediriger vers la page d'accueil
						request.getRequestDispatcher("/").forward(request, response);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (BusinessException e) {
			e.printStackTrace();
		}
	}
}