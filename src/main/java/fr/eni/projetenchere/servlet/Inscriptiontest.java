package fr.eni.projetenchere.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.eni.projetenchere.bll.UserManager;
import fr.eni.projetenchere.bll.CodeErreur;
import fr.eni.projetenchere.bo.User;
import fr.eni.projetenchere.exception.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "inscription", value = "/inscription")
public class Inscriptiontest extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Vérifiez si l'utilisateur est déjà connecté
        if (request.getSession().getAttribute("utilisateurConnecte") != null) {
            // Si oui, redirection vers une autre page ou affichez un message
            request.setAttribute("message", "Vous êtes déjà connecté");
            request.getRequestDispatcher("/WEB-INF/views/accueil.jsp").forward(request, response);
        } else {
            // Si non, affichez la page de connexion
            request.getRequestDispatcher("/WEB-INF/views/inscription.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pseudo = request.getParameter("pseudo");
        String nom = request.getParameter("nom");
        String prenom = request.getParameter("prenom");
        String email = request.getParameter("email");
        String telephone = request.getParameter("telephone");
        String rue = request.getParameter("rue");
        String ville = request.getParameter("ville");
        String codePostal = request.getParameter("codePostal");
        String motDePasse = request.getParameter("motDePasse");

        UserManager userManager = UserManager.getInstance();
        BusinessException businessException = new BusinessException();

        try {
            if (userManager.checkEmailAvailability(email)) {
                businessException.ajouterErreur(CodeErreur.EMAIL_EXISTANT);
            }

            if(userManager.checkPseudoAvailability(pseudo)) {
                businessException.ajouterErreur(CodeErreur.PSEUDO_EXISTANT);
            }

            if(!userManager.validateCodePostal(codePostal)) {
                businessException.ajouterErreur(CodeErreur.CODE_POSTAL_INVALIDE);
            }

            if(!userManager.validateMotDePasse(motDePasse)) {
                businessException.ajouterErreur(CodeErreur.MDP_INVALIDE);
            }

            if (businessException.hasErreurs()) {
                request.setAttribute("listeCodesErreur", businessException.getListeCodesErreur());
                request.getRequestDispatcher("/WEB-INF/views/inscription.jsp").forward(request, response);
            } else {
                User user = new User(pseudo, nom, prenom, email, telephone, rue, ville, codePostal, motDePasse, userManager.DEFAULT_USER_CREDIT, userManager.DEFAULT_IS_ADMIN_VALUE);
                userManager.insertUser(user);
                request.setAttribute("message", "Votre compte a été créé avec succès");
                request.getRequestDispatcher("/").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}