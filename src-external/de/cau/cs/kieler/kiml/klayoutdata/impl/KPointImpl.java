/**
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 * 
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2009 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 *
 * $Id$
 */
package de.cau.cs.kieler.kiml.klayoutdata.impl;

import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.kiml.klayoutdata.KLayoutDataPackage;
import de.cau.cs.kieler.kiml.klayoutdata.KPoint;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>KPoint</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.cau.cs.kieler.kiml.klayoutdata.impl.KPointImpl#getX <em>X</em>}</li>
 *   <li>{@link de.cau.cs.kieler.kiml.klayoutdata.impl.KPointImpl#getY <em>Y</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class KPointImpl extends EObjectImpl implements KPoint {
    /**
     * The default value of the '{@link #getX() <em>X</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getX()
     * @generated
     * @ordered
     */
    protected static final float X_EDEFAULT = 0.0F;

    /**
     * The cached value of the '{@link #getX() <em>X</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getX()
     * @generated
     * @ordered
     */
    protected float x = X_EDEFAULT;

    /**
     * The default value of the '{@link #getY() <em>Y</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getY()
     * @generated
     * @ordered
     */
    protected static final float Y_EDEFAULT = 0.0F;

    /**
     * The cached value of the '{@link #getY() <em>Y</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getY()
     * @generated
     * @ordered
     */
    protected float y = Y_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected KPointImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return KLayoutDataPackage.Literals.KPOINT;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public float getX() {
        return x;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void setX(float newX) {
        float oldX = x;
        x = newX;
        if (newX != oldX && eContainer instanceof KEdgeLayoutImpl) {
            ((KEdgeLayoutImpl) eContainer).modified = true;
        }
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KPOINT__X, oldX, x));
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public float getY() {
        return y;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void setY(float newY) {
        float oldY = y;
        y = newY;
        if (newY != oldY && eContainer instanceof KEdgeLayoutImpl) {
            ((KEdgeLayoutImpl) eContainer).modified = true;
        }
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KPOINT__Y, oldY, y));
        }
    }

    /**
     * <!-- begin-user-doc -->
     * {@inheritDoc}
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void setPos(float newX, float newY) {
        float oldX = x, oldY = y;
        x = newX;
        y = newY;
        if ((newX != oldX || newY != oldY) && eContainer instanceof KEdgeLayoutImpl) {
            ((KEdgeLayoutImpl) eContainer).modified = true;
        }
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KPOINT__X, oldX, x));
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KPOINT__Y, oldY, y));
        }
    }

    /**
     * <!-- begin-user-doc -->
     * {@inheritDoc}
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void applyVector(KVector pos) {
        setPos((float) pos.x, (float) pos.y);
    }

    /**
     * <!-- begin-user-doc -->
     * {@inheritDoc}
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public KVector createVector() {
        return new KVector(x, y);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case KLayoutDataPackage.KPOINT__X:
                return getX();
            case KLayoutDataPackage.KPOINT__Y:
                return getY();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case KLayoutDataPackage.KPOINT__X:
                setX((Float)newValue);
                return;
            case KLayoutDataPackage.KPOINT__Y:
                setY((Float)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case KLayoutDataPackage.KPOINT__X:
                setX(X_EDEFAULT);
                return;
            case KLayoutDataPackage.KPOINT__Y:
                setY(Y_EDEFAULT);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case KLayoutDataPackage.KPOINT__X:
                return x != X_EDEFAULT;
            case KLayoutDataPackage.KPOINT__Y:
                return y != Y_EDEFAULT;
        }
        return super.eIsSet(featureID);
    }
    
    /**
     * {@inheritDoc}
     * @generated NOT
     */
    @Override
    protected void eBasicSetContainer(InternalEObject newContainer, int newContainerFeatureID) {
        if (eContainer instanceof KEdgeLayoutImpl) {
            ((KEdgeLayoutImpl) eContainer).modified = true;
        }
        super.eBasicSetContainer(newContainer, newContainerFeatureID);
        if (newContainer instanceof KEdgeLayoutImpl) {
            ((KEdgeLayoutImpl) newContainer).modified = true;
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated NOT
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

} //KPointImpl
