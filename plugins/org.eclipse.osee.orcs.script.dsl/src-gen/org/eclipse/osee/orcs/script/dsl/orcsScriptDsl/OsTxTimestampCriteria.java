/**
 */
package org.eclipse.osee.orcs.script.dsl.orcsScriptDsl;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Os Tx Timestamp Criteria</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsTxTimestampCriteria#getClause <em>Clause</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OrcsScriptDslPackage#getOsTxTimestampCriteria()
 * @model
 * @generated
 */
public interface OsTxTimestampCriteria extends OsTxCriteria {
   /**
    * Returns the value of the '<em><b>Clause</b></em>' containment reference. <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Clause</em>' containment reference isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Clause</em>' containment reference.
    * @see #setClause(OsTxTimestampClause)
    * @see org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OrcsScriptDslPackage#getOsTxTimestampCriteria_Clause()
    * @model containment="true"
    * @generated
    */
   OsTxTimestampClause getClause();

   /**
    * Sets the value of the '{@link org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsTxTimestampCriteria#getClause
    * <em>Clause</em>}' containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param value the new value of the '<em>Clause</em>' containment reference.
    * @see #getClause()
    * @generated
    */
   void setClause(OsTxTimestampClause value);

} // OsTxTimestampCriteria
