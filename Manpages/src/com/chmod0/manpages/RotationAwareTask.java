/**
 * Classe abstraite qui permet de créer une tâche (un thread) liée à une activité
 * Pour éviter les crashs, on doit détacher l'activité de la tâche avant de la relancer (en cas de rotation par ex.)
 * La tâche est sauvegardée en mémoire, on peut donc lui attacher une nouvelle activité par la suite
 * @author Julien Guepin 
 * @author Guillaume Gérard
 */

package com.chmod0.manpages;

import android.os.AsyncTask;

public abstract class RotationAwareTask<A, B, C> extends AsyncTask<A, B, C> {
	
	// Activité attachée à la tâche
	ManualActivity activity = null;
	
	/**
	 * Constructeur qui créé une tâche liée à une activité
	 * @param activity activité à lier à la tâche
	 */
	public RotationAwareTask(ManualActivity activity) {
		attach(activity);
	}
	
	/**
	 * Détache la tâche de son activité
	 */
	public void detach() {
		this.activity = null;
	}
	
	/**
	 * Attacher une activité à la tâche
	 * @param activity activité à attacher
	 */
	public void attach(ManualActivity activity) {
		this.activity = activity;
	}

}
