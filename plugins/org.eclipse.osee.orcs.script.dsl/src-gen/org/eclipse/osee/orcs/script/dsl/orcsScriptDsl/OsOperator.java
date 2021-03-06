/**
 */
package org.eclipse.osee.orcs.script.dsl.orcsScriptDsl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc --> A representation of the literals of the enumeration '<em><b>Os Operator</b></em>', and
 * utility methods for working with them. <!-- end-user-doc -->
 * 
 * @see org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OrcsScriptDslPackage#getOsOperator()
 * @model
 * @generated
 */
public enum OsOperator implements Enumerator {
   /**
    * The '<em><b>EQUAL</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #EQUAL_VALUE
    * @generated
    * @ordered
    */
   EQUAL(0, "EQUAL", "="),

   /**
    * The '<em><b>NOT EQUAL</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #NOT_EQUAL_VALUE
    * @generated
    * @ordered
    */
   NOT_EQUAL(1, "NOT_EQUAL", "!="),

   /**
    * The '<em><b>LESS THAN</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #LESS_THAN_VALUE
    * @generated
    * @ordered
    */
   LESS_THAN(2, "LESS_THAN", "<"),

   /**
    * The '<em><b>LESS THAN EQ</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #LESS_THAN_EQ_VALUE
    * @generated
    * @ordered
    */
   LESS_THAN_EQ(3, "LESS_THAN_EQ", "<="),

   /**
    * The '<em><b>GREATER THAN</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #GREATER_THAN_VALUE
    * @generated
    * @ordered
    */
   GREATER_THAN(4, "GREATER_THAN", ">"),

   /**
    * The '<em><b>GREATER THAN EQ</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #GREATER_THAN_EQ_VALUE
    * @generated
    * @ordered
    */
   GREATER_THAN_EQ(5, "GREATER_THAN_EQ", ">=");

   /**
    * The '<em><b>EQUAL</b></em>' literal value. <!-- begin-user-doc -->
    * <p>
    * If the meaning of '<em><b>EQUAL</b></em>' literal object isn't clear, there really should be more of a description
    * here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @see #EQUAL
    * @model literal="="
    * @generated
    * @ordered
    */
   public static final int EQUAL_VALUE = 0;

   /**
    * The '<em><b>NOT EQUAL</b></em>' literal value. <!-- begin-user-doc -->
    * <p>
    * If the meaning of '<em><b>NOT EQUAL</b></em>' literal object isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @see #NOT_EQUAL
    * @model literal="!="
    * @generated
    * @ordered
    */
   public static final int NOT_EQUAL_VALUE = 1;

   /**
    * The '<em><b>LESS THAN</b></em>' literal value. <!-- begin-user-doc -->
    * <p>
    * If the meaning of '<em><b>LESS THAN</b></em>' literal object isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @see #LESS_THAN
    * @model literal="<"
    * @generated
    * @ordered
    */
   public static final int LESS_THAN_VALUE = 2;

   /**
    * The '<em><b>LESS THAN EQ</b></em>' literal value. <!-- begin-user-doc -->
    * <p>
    * If the meaning of '<em><b>LESS THAN EQ</b></em>' literal object isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @see #LESS_THAN_EQ
    * @model literal="<="
    * @generated
    * @ordered
    */
   public static final int LESS_THAN_EQ_VALUE = 3;

   /**
    * The '<em><b>GREATER THAN</b></em>' literal value. <!-- begin-user-doc -->
    * <p>
    * If the meaning of '<em><b>GREATER THAN</b></em>' literal object isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @see #GREATER_THAN
    * @model literal=">"
    * @generated
    * @ordered
    */
   public static final int GREATER_THAN_VALUE = 4;

   /**
    * The '<em><b>GREATER THAN EQ</b></em>' literal value. <!-- begin-user-doc -->
    * <p>
    * If the meaning of '<em><b>GREATER THAN EQ</b></em>' literal object isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @see #GREATER_THAN_EQ
    * @model literal=">="
    * @generated
    * @ordered
    */
   public static final int GREATER_THAN_EQ_VALUE = 5;

   /**
    * An array of all the '<em><b>Os Operator</b></em>' enumerators. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   private static final OsOperator[] VALUES_ARRAY =
      new OsOperator[] {EQUAL, NOT_EQUAL, LESS_THAN, LESS_THAN_EQ, GREATER_THAN, GREATER_THAN_EQ,};

   /**
    * A public read-only list of all the '<em><b>Os Operator</b></em>' enumerators. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @generated
    */
   public static final List<OsOperator> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

   /**
    * Returns the '<em><b>Os Operator</b></em>' literal with the specified literal value. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @generated
    */
   public static OsOperator get(String literal) {
      for (int i = 0; i < VALUES_ARRAY.length; ++i) {
         OsOperator result = VALUES_ARRAY[i];
         if (result.toString().equals(literal)) {
            return result;
         }
      }
      return null;
   }

   /**
    * Returns the '<em><b>Os Operator</b></em>' literal with the specified name. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @generated
    */
   public static OsOperator getByName(String name) {
      for (int i = 0; i < VALUES_ARRAY.length; ++i) {
         OsOperator result = VALUES_ARRAY[i];
         if (result.getName().equals(name)) {
            return result;
         }
      }
      return null;
   }

   /**
    * Returns the '<em><b>Os Operator</b></em>' literal with the specified integer value. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @generated
    */
   public static OsOperator get(int value) {
      switch (value) {
         case EQUAL_VALUE:
            return EQUAL;
         case NOT_EQUAL_VALUE:
            return NOT_EQUAL;
         case LESS_THAN_VALUE:
            return LESS_THAN;
         case LESS_THAN_EQ_VALUE:
            return LESS_THAN_EQ;
         case GREATER_THAN_VALUE:
            return GREATER_THAN;
         case GREATER_THAN_EQ_VALUE:
            return GREATER_THAN_EQ;
      }
      return null;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   private final int value;

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   private final String name;

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   private final String literal;

   /**
    * Only this class can construct instances. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   private OsOperator(int value, String name, String literal) {
      this.value = value;
      this.name = name;
      this.literal = literal;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public int getValue() {
      return value;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String getName() {
      return name;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String getLiteral() {
      return literal;
   }

   /**
    * Returns the literal value of the enumerator, which is its string representation. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String toString() {
      return literal;
   }

} //OsOperator
